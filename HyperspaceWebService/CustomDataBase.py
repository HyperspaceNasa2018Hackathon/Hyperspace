import pytz
from datetime import datetime
import MySQLdb

from azure.storage.blob import BlockBlobService 
from azure.common import AzureMissingResourceHttpError
# from datetime import datetime,timezone

from ShareData import CustomString
from CustomConf import AzureStorageConf,MySQLConf #,JobInfoConf


class MySqlDataBase():
    def __init__(self, conf):
        self.db=None
        self.myC=None
        self.result=None
        self.errorMsg=''
        self.s=CustomString()
        if (isinstance(conf, MySQLConf)):
            self.myC=conf
    def getRowToJsonString(self, row, column_names):
        out=''
        if (isinstance(row, tuple)):
            for i in range(0, len(column_names)):
                if (out!=''): out+=self.s.comma()
                if (isinstance(row[i],str)):
                    col_str=row[i]
                    #out+='"'+field_names[i]+'"'+":"+'"'+col+'"'+
                elif (isinstance(row[i],int)):
                    col_str=str(row[i])
                elif (isinstance(row[i],float)):
                    col_str=str(row[i])
                elif (isinstance(row[i],datetime)):
                    local = pytz.timezone ('UTC')
                    naive = datetime.strptime (row[i].strftime("%Y-%m-%d %H:%M:%S"),"%Y-%m-%d %H:%M:%S")
                    local_dt = local.localize(naive, is_dst=None)
                    utc_dt = local_dt.astimezone (pytz.timezone('UTC'))
                    out+=str(utc_dt.strftime("%Y-%m-%d %H:%M:%S"))
                    col_str=str(utc_dt.strftime("%Y-%m-%d"))
                elif (isinstance(row[i], type(None))):
                    col_str=str('')
                out+='"'+column_names[i]+'"'+":"+'"'+col_str+'"'
        out='{'+out+'}'
        return out

    def getRowToString(self, row, delimiter='\t'):
        out=''
        if (isinstance(row, tuple)):
            for col in row:
                if (out!=''): out+=delimiter
                if (isinstance(col,str)):
                    out+=col
                elif (isinstance(col,int)):
                    out+=str(col)
                elif (isinstance(col,float)):
                    out+=str('%.6f'  % col)
                elif (isinstance(col,datetime)):
                    local = pytz.timezone ('Asia/Taipei')
                    naive = datetime.strptime (col.strftime("%Y-%m-%d %H:%M:%S"),"%Y-%m-%d %H:%M:%S")
                    local_dt = local.localize(naive, is_dst=None)
                    utc_dt = local_dt.astimezone (pytz.timezone('Asia/Taipei'))
                    out+=str(utc_dt.strftime("%Y-%m-%d %H:%M:%S"))
                elif (isinstance(col, type(None))):
                    out+=str('')
        return out
    def getErrorMsg(self): return self.errorMsg
    def connectDB(self):
        isSuccess=False;
        if (self.myC is None): return isSuccess
        if (self.myC.hasNull()): return isSuccess
        try:
            self.db = MySQLdb.connect(host=self.myC.getMySqlDbIP(), port=int(self.myC.getMySqlDbPort()) ,user=self.myC.getMySqlDbUser(), passwd=self.myC.getMySqlDbPassword(), db=self.myC.getMySqlDbName())
            self.result = self.db.cursor()
            isSuccess=True
        except Exception as e:
            self.errorMsg=str(type(e))+self.s.lf()+str(e)
        return isSuccess
    def close(self): 
        if (not self.result is None):
            try:
                self.result.close()
            except Exception as e:
                err=str(type(e))+self.s.lf()+str(e)
                self.errorMsg=err if (self.errorMsg == '') else self.errorMsg+self.s.lf()+err
        if (not self.db is None): 
            try:
                self.db.close()
            except Exception as e:
                err=str(type(e))+self.s.lf()+str(e)
                self.errorMsg=err if (self.errorMsg == '') else self.errorMsg+self.s.lf()+err
    def getResult(self): return self.result
    def get_tw_data(self,StartDate,EndDate,Confidence=30):
        isSuccess=False;
        if (self.db is None): return isSuccess
        if (self.myC is None): return isSuccess
        if (self.result is None): return isSuccess
        self.myC.getColumnList()
        import MySQLdb
        querry_str='SELECT latitude, longitude, brightness, acq_date, acq_time, confidence, bright_t31, frp, daynight FROM FIRMS.MODIS_World '
        querry_str+='where acq_date>="{0}" AND acq_date<= "{1}" and confidence>={2}'.format('2018-10-14','2018-10-21',str(Confidence))
        #querry_str+='where acq_date>="{0}" AND acq_date<= "{1}" and confidence>={2}'.format(StartDate,EndDate,str(Confidence))
        try:
            self.result.execute(querry_str)
            isSuccess=True
        except Exception as e:
            err=str(type(e))+self.s.lf()+str(e)
            self.errorMsg=err if (self.errorMsg == '') else self.errorMsg+self.s.lf()+err
        return isSuccess
    def get_near_real_time(self):
        isSuccess=False;
        if (self.db is None): return isSuccess
        if (self.myC is None): return isSuccess
        if (self.result is None): return isSuccess
        self.myC.getColumnList()
        import MySQLdb
        querry_str='SELECT latitude, longitude, temperature, acq_date, acq_time, confidence, resource, country, time_status FROM FIRMS.staging_nrt'
        try:
            self.result.execute(querry_str)
            isSuccess=True
        except Exception as e:
            err=str(type(e))+self.s.lf()+str(e)
            print(err)
            self.errorMsg=err if (self.errorMsg == '') else self.errorMsg+self.s.lf()+err
        return isSuccess
    def insert_nrt_tmp(self, latitude, longitude, temperature, acq_date, acq_time, confidence, country, time_status):
        isSuccess=False;
        if (self.db is None): return isSuccess
        if (self.myC is None): return isSuccess
        if (self.result is None): return isSuccess
        self.myC.getColumnList()
        import MySQLdb
        querry_str='INSERT INTO hyperspace_firms_tmp (latitude, longitude, temperature, acq_date, acq_time, confidence, resource, country, time_status) VALUES ({latitude}, {longitude}, {temperature}, "{acq_date}", "{acq_time}", {confidence}, "MODIS", "{country}", "{time_status}")'
        querry_str=querry_str.format(latitude=latitude, longitude=longitude, temperature=temperature,\
                                acq_date=acq_date, acq_time=acq_time, confidence=confidence, country=country, time_status=time_status)
        try:
            self.result.execute(querry_str)
            db.commit()
            isSuccess=True
        except Exception as e:
            err=str(type(e))+self.s.lf()+str(e)
            print(err)
            self.errorMsg=err if (self.errorMsg == '') else self.errorMsg+self.s.lf()+err
        return isSuccess
    def insert_report(self ):#need fix
        isSuccess=False;
        if (self.db is None): return isSuccess
        if (self.myC is None): return isSuccess
        if (self.result is None): return isSuccess
        self.myC.getColumnList()
        import MySQLdb
        querry_str='INSERT INTO hyperspace_firms_tmp (latitude, longitude, temperature, acq_date, acq_time, confidence, resource, country, time_status) VALUES ({latitude}, {longitude}, {temperature}, "{acq_date}", "{acq_time}", {confidence}, "MODIS", "{country}", "{time_status}")'
        querry_str=querry_str.format(latitude=latitude, longitude=longitude, temperature=temperature,\
                                acq_date=acq_date, acq_time=acq_time, confidence=confidence, country=country, time_status=time_status)
        try:
            self.result.execute(querry_str)
            db.commit()
            isSuccess=True
        except Exception as e:
            err=str(type(e))+self.s.lf()+str(e)
            print(err)
            self.errorMsg=err if (self.errorMsg == '') else self.errorMsg+self.s.lf()+err
        return isSuccess
    
class AzureStorage():
    def __init__(self, conf):
        self.blob_service=None
        self.asc=None
        self.errorMsg=''
        self.s=CustomString()
        if (isinstance(conf, AzureStorageConf)):
            self.asc=conf
    def connectStorage(self):
        isSuccess=False;
        if (self.asc is None): return isSuccess
        if (self.asc.hasNull()): return isSuccess
        try:
            self.blob_service = BlockBlobService(account_name=self.asc.getAzureStorageAccountName(),
                                                 account_key=self.asc.getAzureStorageAccountKey())
            self.blob_service = self.blob_service.list_blobs()
            isSuccess=True
        except Exception as e:
            self.errorMsg=str(type(e))+self.s.lf()+str(e)
        return isSuccess
    def get_prefix_files_list(self,prefix_name):
        isSuccess=False;
        if (self.asc is None): return isSuccess
        if (self.asc.hasNull()): return isSuccess
        blob_list=[]
        try:
            blob_list_obj = self.blob_service.list_blobs(self.asc.getAzureStorageContainer(), prefix=prefix_name)
            blob_list = [x.name for x in blob_list_obj.blobs] if len(blob_list_obj.blobs) > 0 else []
            isSuccess=True
        except AzureMissingResourceHttpError as e:
            self.errorMsg=str(type(e))+self.s.lf()+str(e)
        except Exception as e:
            self.errorMsg=str(type(e))+self.s.lf()+str(e)
        if (isSuccess):
            return blob_list
        else:
            return []
    def put_blbo(self,):
        self.blob_service.create_blob_from_path(self.asc.getAzureStorageContainer, local_file_name, ''  )

        
    def get_tmp(self):
        ''
    def get(self):
        isSuccess=False;
        if (self.asc is None): return isSuccess
        if (self.asc.hasNull()): return isSuccess
        try:
            self.blob_service = BlockBlobService(account_name=self.asc.getAzureStorageAccountName(),
                                                 account_key=self.asc.getAzureStorageAccountKey())
            self.blob_service.list
            isSuccess=True
        except Exception as e:
            self.errorMsg=str(type(e))+self.s.lf()+str(e)
        return isSuccess
lat=-13.781
