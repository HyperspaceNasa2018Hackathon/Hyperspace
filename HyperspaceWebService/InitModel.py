import os,sys
import logging
from logging import config
import yaml
import time
import requests

from ShareData import PtternIndex,CustomString
from CustomConf import AzureStorageConf
from CustomConf import MySQLConf


class SendRequest():
    def __init__(self):
        self.okTag='OK'
        self.failedTag='FAILED'
        self.domainName='https://staging-ireport.abc-atec.com'
        self.apiUrl= None
        self.contentType= None
    def setStagingDomain(self): self.domainName='https://staging-ireport.abc-atec.com'
    def setPredictionDomain(self): self.domainName='https://ireport.abc-atec.com'
    def setLoggingApiURL(self, source, action, target):
        urlFormat='/ireport/api/v1.0/logging/{source}/{action}/{target}'\
                        .format(source=source, action=action, target=target)
        self.apiUrl=urlFormat
    def setFormUrlencodedContentType(self):self.contentType={'Content-Type':'application/x-www-form-urlencoded'}
    def send(self, data, retry_max_count=10, retry_delay_minute =3):
        url=self.domainName+self.apiUrl
        isSuccess=False
        requestCount=0
        delay_time=retry_delay_minute *60
        history_str=''
        while (not isSuccess):
            requestCount+=1
            response=requests.post(url=url,data=data,headers=self.contentType)
            
            if ((response.status_code==200) or (response.status_code==201)):
                if (response.text == self.okTag) : break
            
            if (isSuccess): break
            if (requestCount == retry_max_count): break 
            time.sleep(delay_time)
        return response
        
    
class timer():
    def __init__(self):
        self.timeflag=time.time()
        self.timenow=time.time()
    def setTimeFlag(self): self.timeflag=time.time()
    def getSepndTime(self):
        self.timenow=time.time()
        logT=(self.timenow-self.timeflag)
        out=''
        if (logT<1):
            out='%0.3f ms' % ( (self.timenow-self.timeflag)*1000.0 )
        elif (logT< (60) ):
            out='%0.3f secs' % ( (self.timenow-self.timeflag) )
        elif (logT< (60*60) ):
            out='%0.3f mins' % ( (self.timenow-self.timeflag)/60 )
        else:
            out='%0.3f hrs' % ( (self.timenow-self.timeflag)/60/60 )
        self.timeflag=time.time()
        return out

class log():
    def __init__(self, confPath,classsName=None):
        self.conf=None
        self.classsName=None
        with open(confPath, 'r', encoding='utf8') as f:
            self.conf = yaml.load(f)  # @UndefinedVariable
        f.close()
        config.dictConfig(self.conf)
    def fatal(self, msg, *args, **kwargs):
        getLogStr='fatal'  if (self.classsName is None) else ('fatal.'+self.classsName)
        fatal_logger = logging.getLogger(getLogStr)
        fatal_logger.fatal(msg, *args, **kwargs)
        sys.exit()
    def error(self, msg, *args, **kwargs):
        getLogStr='error'  if (self.classsName is None) else ('error.'+self.classsName)
        error_logger = logging.getLogger(getLogStr)
        error_logger.error(msg, *args, **kwargs)
        sys.exit()
    def warn(self, msg, *args, **kwargs):
        getLogStr='warn'  if (self.classsName is None) else ('warn.'+self.classsName)
        warn_logger = logging.getLogger(getLogStr)
        warn_logger.warn(msg, *args, **kwargs)
    def info(self, msg, *args, **kwargs):
        getLogStr='info'  if (self.classsName is None) else ('info.'+self.classsName)
        info_logger = logging.getLogger(getLogStr)
        info_logger.info(msg, *args, **kwargs)
    def debug(self, msg, *args, **kwargs):
        getLogStr='debug'  if (self.classsName is None) else ('debug.'+self.classsName)
        debug_logger = logging.getLogger(getLogStr)
        debug_logger.debug(msg, *args, **kwargs)
        
class InitModel:
    def __init__(self):
        from ConfParser import Parser
        self.__isContinue=True
        self.p=Parser()
        self.s=CustomString()
        self.asc=None
        self.myC=None
        self.logConfPath=None    
    def isLogConfPathNone(self):
        from pathlib import Path
        flag=(self.logConfPath is None)
        logConf = Path(self.logConfPath)
        if (not flag):
            if (not logConf.exists()): flag=True
        return flag
    def getLogConfPath(self): return self.logConfPath
    
    def isAzureStorageConfNone(self): return (self.asc is None)
    def isAzureStorageConfHasNull(self): return self.asc.hasNull()
    def getAzureStorageConf(self): return self.asc
    
    def isMySQLConfNone(self): return (self.myC is None)
    def isMySQLConfHasNull(self): return (self.myC.hasNull())
    def getMySQLConf(self): return self.myC
    
    def setConf(self, confPath, clazzName='init'):
        from pathlib import Path
        def isBetween(x, lower, upper): return lower <= x and x <= upper
        confFile = Path(confPath)
        if (not confFile.exists()):
            print('ConfFile('+confPath+') is not exists')
            return
        else:
            with open(confPath) as file:  
                while True:
                    line = file.readline()
                    if (not line): break
                    if (line is None): continue
                    if (line == ''): continue
                    line=line.rstrip('\r\n').rstrip('\n')
                    pi =self.p.getPatternIndex(line)
                    if (pi == -1): continue
                    searchStr=self.p.getSerachStr()
                    if (searchStr is None): continue
                    if (searchStr==''): continue
                    if (pi==PtternIndex.Log.PI_LOG_CONFIG_FILE_PATH):
                        self.logConfPath=searchStr;
                    elif (isBetween(pi, 101, 105)):
                        if (self.asc is None): self.asc=AzureStorageConf()
                        if (pi==PtternIndex.AzureStorage.PI_AZURE_STORAGE_ACCOUNTNAME): self.asc.setAzureStorageAccountName(searchStr)
                        if (pi==PtternIndex.AzureStorage.PI_AZURE_STORAGE_PROTOCOL): self.asc.setAzureStorageProtocol(searchStr)
                        if (pi==PtternIndex.AzureStorage.PI_AZURE_STORAGE_ACCOUNTKEY): self.asc.setAzureStorageAccountKey(searchStr)
                        if (pi==PtternIndex.AzureStorage.PI_AZURE_STORAGE_CONTAINER): self.asc.setAzureStorageContainer(searchStr)
                        if (pi==PtternIndex.AzureStorage.PI_AZURE_STORAGE_FILENAME): self.asc.setAzureStorageFileName(searchStr)
                    elif (isBetween(pi, 201, 207)):
                        #MySQLConf
                        if (self.myC is None): self.myC=MySQLConf()
                        if (pi==PtternIndex.Mysql.PI_MYSQL_DB_IP): self.myC.setMySqlDbIP(searchStr)
                        if (pi==PtternIndex.Mysql.PI_MYSQL_DB_PORT):  self.myC.setMySqlDbPort(searchStr)
                        if (pi==PtternIndex.Mysql.PI_MYSQL_DB_NAME): self.myC.setMySqlDbName(searchStr)
                        if (pi==PtternIndex.Mysql.PI_MYSQL_DB_USER): self.myC.setMySqlDbUser(searchStr)
                        if (pi==PtternIndex.Mysql.PI_MYSQL_DB_PASSWORD): self.myC.setMySqlDbPassword(searchStr)
                        if (pi==PtternIndex.Mysql.PI_MYSQL_TABLE_NAME): self.myC.setMySqlTableName(searchStr)
                        if (pi==PtternIndex.Mysql.PI_MYSQL_COLUMN_LIST): self.myC.setColumnList(searchStr)
                    else:
                        print('unknow PatternIndex({0})'.format(pi))
            file.close()
    def isContinue(self): return self.__isContinue
    
    def cleanFile(self, fileName, outputPath):
        isSuccess=False;
        if (not outputPath.endswith(self.s.slash())): outputPath+=self.s.slash()
        if (not os.path.exists(outputPath)): os.makedirs(outputPath)
        try:
            open(outputPath+fileName, 'w', encoding='utf8').close()
            isSuccess=True
        except Exception as e:
            self.errorMs=type(e)+self.s.lf()+e
            print(self.errorMs)
        return isSuccess
    def outputFile(self, contents, fileName, outputPath, append=True):
        isSuccess=False;
        if (not outputPath.endswith(self.s.slash())): outputPath+=self.s.slash()
        if (not os.path.exists(outputPath)): os.makedirs(outputPath)
        if (not contents.endswith(self.s.lf())): contents+=self.s.lf()
        try:
            modeStr='a' if (append) else 'w'
            with open(outputPath+fileName, modeStr, encoding='utf8') as file:
                file.write(contents);
            isSuccess=True
        except Exception as e:
            self.errorMs=type(e)+self.s.lf()+e
            print(self.errorMs)
        return isSuccess
    
    def changeConf(self, index, confPath, newConfStr):
        import subprocess
        patternStr=self.p.getPatternStr(index)
        newStr=patternStr.replace("(.+)", newConfStr)
        patternStr=patternStr.replace("(.+)", '.*')
        shell_cmd="sed -i 's/{0}/{1}/g' '{2}'".format(patternStr, newStr, confPath)
        print(shell_cmd)
        subprocess.call(shell_cmd, shell=True)
        
        
        
    #String contents, String fileName,String outputPath
                
