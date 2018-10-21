class ENV:
    __TEST_ENV_STR='TEST'
    __STAGE_ENV_STR='STAGE'
    __PRODUCTION_ENV_STR='PRODUCTION'
    def getTestEnvStr(self):return self.__TEST_ENV_STR;
    def getStageEnvStr(self):return self.__STAGE_ENV_STR;
    def getProductionEnvStr(self):return self.__PRODUCTION_ENV_STR;

class AzureStorageConf:
    def __init__(self):
        self.__AZURE_STORAGE_ACCOUNTNAME=''
        self.__AZURE_STORAGE_PROTOCOL=''
        self.__AZURE_STORAGE_ACCOUNTKEY=''
        self.__AZURE_STORAGE_CONTAINER=''
        self.__AZURE_STORAGE_FILENAME=''
        
    def setAzureStorageAccountName(self, Str): self.__AZURE_STORAGE_ACCOUNTNAME=Str
    def setAzureStorageProtocol(self, Str): self.__AZURE_STORAGE_PROTOCOL=Str
    def setAzureStorageAccountKey(self,Str): self.__AZURE_STORAGE_ACCOUNTKEY=Str
    def setAzureStorageContainer(self, Str): self.__AZURE_STORAGE_CONTAINER=Str
    def setAzureStorageFileName(self, Str): self.__AZURE_STORAGE_FILENAME=Str
    
    def getAzureStorageAccountName(self):return self.__AZURE_STORAGE_ACCOUNTNAME
    def getAzureStorageProtocol(self):return self.__AZURE_STORAGE_PROTOCOL
    def getAzureStorageAccountKey(self):return self.__AZURE_STORAGE_ACCOUNTKEY
    def getAzureStorageContainer(self):return self.__AZURE_STORAGE_CONTAINER
    def getAzureStorageFileName(self):return self.__AZURE_STORAGE_FILENAME
    def hasNull(self):
        flag=False
        flag =flag or (self.__AZURE_STORAGE_ACCOUNTNAME =='')
        flag =flag or (self.__AZURE_STORAGE_PROTOCOL =='')
        flag =flag or (self.__AZURE_STORAGE_ACCOUNTKEY =='')
        flag =flag or (self.__AZURE_STORAGE_CONTAINER =='')
        return flag
    def toString(self):
        out=''
        out+=("[AZURE_STORAGE_ACCOUNTNAME]=null") if (self.__AZURE_STORAGE_ACCOUNTNAME =='') else ("[AZURE_STORAGE_ACCOUNTNAME]="+self.__AZURE_STORAGE_ACCOUNTNAME)
        out+='\n'
        out+=("[AZURE_STORAGE_PROTOCOL]=null") if (self.__AZURE_STORAGE_PROTOCOL =='') else ("[AZURE_STORAGE_PROTOCOL]="+self.__AZURE_STORAGE_PROTOCOL)
        out+='\n'
        out+=("[AZURE_STORAGE_ACCOUNTKEY]=null") if (self.__AZURE_STORAGE_ACCOUNTKEY =='') else ("[AZURE_STORAGE_ACCOUNTKEY]="+self.__AZURE_STORAGE_ACCOUNTKEY)
        out+='\n'
        out+=("[AZURE_STORAGE_CONTAINER]=null") if (self.__AZURE_STORAGE_CONTAINER =='') else ("[AZURE_STORAGE_CONTAINER]="+self.__AZURE_STORAGE_CONTAINER)
        out+='\n'
        out+=("[AZURE_STORAGE_FILENAME]=null") if (self.__AZURE_STORAGE_FILENAME =='') else ("[AZURE_STORAGE_FILENAME]="+self.__AZURE_STORAGE_FILENAME)
        out+='\n'
        return out

class MySQLConf:
    def __init__(self):
        self.__MYSQL_DB_IP=''
        self.__MYSQL_DB_PORT=''
        self.__MYSQL_DB_NAME=''
        self.__MYSQL_DB_USER=''
        self.__MYSQL_DB_PASSWORD=''
        self.__MYSQL_TABLE_NAME=''
        self.__COLUMN_LIST=''
        
    def setMySqlDbIP(self, Str): self.__MYSQL_DB_IP=Str
    def setMySqlDbPort(self, Str): self.__MYSQL_DB_PORT=Str
    def setMySqlDbName(self, Str): self.__MYSQL_DB_NAME=Str
    def setMySqlDbUser(self, Str): self.__MYSQL_DB_USER=Str
    def setMySqlDbPassword(self, Str): self.__MYSQL_DB_PASSWORD=Str
    def setMySqlTableName(self, Str): self.__MYSQL_TABLE_NAME=Str
    def setColumnList(self, Str): self.__COLUMN_LIST=Str

    def getMySqlDbIP(self): return self.__MYSQL_DB_IP
    def getMySqlDbPort(self): return self.__MYSQL_DB_PORT
    def getMySqlDbName(self): return self.__MYSQL_DB_NAME
    def getMySqlDbUser(self): return self.__MYSQL_DB_USER
    def getMySqlDbPassword(self): return self.__MYSQL_DB_PASSWORD
    def getMySqlTableName(self): return self.__MYSQL_TABLE_NAME
    def getColumnList(self): return self.__COLUMN_LIST
    
    def hasNull(self):
        flag=False
        flag =flag or (self.__MYSQL_DB_IP =='')
        flag =flag or (self.__MYSQL_DB_PORT =='')
        flag =flag or (self.__MYSQL_DB_NAME =='')
        flag =flag or (self.__MYSQL_DB_USER =='')
        flag =flag or (self.__MYSQL_DB_PASSWORD =='')
        return flag
    def toString(self):
        out=''
        out+=("[MYSQL_DB_IP]=null") if (self.__MYSQL_DB_IP =='') else ("[MYSQL_DB_IP]="+self.__ORACLE_DB_IP)
        out+='\n'
        out+=("[MYSQL_DB_PORT]=null") if (self.__MYSQL_DB_PORT =='') else ("[MYSQL_DB_PORT]="+self.__ORACLE_DB_PORT)
        out+='\n'
        out+=("[MYSQL_DB_NAME]=null") if (self.__MYSQL_DB_NAME =='') else ("[MYSQL_DB_NAME]="+self.__ORACLE_DB_SERVICE_NAME)
        out+='\n'
        out+=("[MYSQL_DB_USER]=null") if (self.__MYSQL_DB_USER =='') else ("[MYSQL_DB_USER]="+self.__ORACLE_DB_USER)
        out+='\n'
        out+=("[MYSQL_DB_PASSWORD]=null") if (self.__MYSQL_DB_PASSWORD =='') else ("[MYSQL_DB_PASSWORD]="+self.__ORACLE_DB_PASSWORD)
        out+='\n'
        out+=("[MYSQL_TABLE_NAME]=null") if (self.__MYSQL_TABLE_NAME =='') else ("[MYSQL_TABLE_NAME]="+self.__ORACLE_TABLE_NAME)
        out+='\n'
        out+=("[MYSQL_COLUMN_LIST]=null") if (self.__COLUMN_LIST =='') else ("[MYSQL_COLUMN_LIST]="+self.__COLUMN_LIST)
        out+='\n'
        return out
    