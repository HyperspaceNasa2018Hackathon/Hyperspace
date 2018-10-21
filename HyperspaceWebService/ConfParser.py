from ShareData import PtternIndex

class Parser:
    def __init__(self):
        self.ptterns={}
        def setPtterns():
#             print("set LogConfig Patterns..")
            self.ptterns[PtternIndex.Log.PI_LOG_CONFIG_FILE_PATH] = "\\["+"LOG_CONFIG_FILE_PATH"+"\\]=(.+)"
#             print("set AzureStorage Patterns..")
            self.ptterns[PtternIndex.AzureStorage.PI_AZURE_STORAGE_ACCOUNTNAME] = "\\["+"AZURE_STORAGE_ACCOUNTNAME"+"\\]=(.+)"
            self.ptterns[PtternIndex.AzureStorage.PI_AZURE_STORAGE_PROTOCOL] = "\\["+"AZURE_STORAGE_PROTOCOL"+"\\]=(.+)"
            self.ptterns[PtternIndex.AzureStorage.PI_AZURE_STORAGE_ACCOUNTKEY] = "\\["+"AZURE_STORAGE_ACCOUNTKEY"+"\\]=(.+)"
            self.ptterns[PtternIndex.AzureStorage.PI_AZURE_STORAGE_CONTAINER] = "\\["+"AZURE_STORAGE_CONTAINER"+"\\]=(.+)"
            self.ptterns[PtternIndex.AzureStorage.PI_AZURE_STORAGE_FILENAME] = "\\["+"AZURE_STORAGE_FILENAME"+"\\]=(.+)"
#             print("set MySQL Patterns..")
            self.ptterns[PtternIndex.Mysql.PI_MYSQL_DB_IP] = "\\["+"MYSQL_DB_IP"+"\\]=(.+)"
            self.ptterns[PtternIndex.Mysql.PI_MYSQL_DB_PORT] = "\\["+"MYSQL_DB_PORT"+"\\]=(.+)"
            self.ptterns[PtternIndex.Mysql.PI_MYSQL_DB_NAME] = "\\["+"MYSQL_DB_NAME"+"\\]=(.+)"
            self.ptterns[PtternIndex.Mysql.PI_MYSQL_DB_USER] = "\\["+"MYSQL_DB_USER"+"\\]=(.+)"
            self.ptterns[PtternIndex.Mysql.PI_MYSQL_DB_PASSWORD] = "\\["+"MYSQL_DB_PASSWORD"+"\\]=(.+)"
            self.ptterns[PtternIndex.Mysql.PI_MYSQL_TABLE_NAME] = "\\["+"MYSQL_TABLE_NAME"+"\\]=(.+)"
            self.ptterns[PtternIndex.Mysql.PI_MYSQL_COLUMN_LIST] = "\\["+"MYSQL_COLUMN_LIST"+"\\]=(.+)"
        setPtterns()
        self.serachStr=None
    def getPatternIndex(self,Str):
        import re
        index=-1
        for i in self.ptterns.keys():
            patStr=self.ptterns[i]
            match = re.match(patStr, Str)
            if (match != None):
                index=i
                self.serachStr=match.group(1)
                break
        return index
    def getPatternStr(self,index): return self.ptterns.get(index) 
    def getSerachStr(self): return self.serachStr
