class PtternIndex:
    class Log:
        PI_LOG_CONFIG_FILE_PATH=0
    class AzureStorage:
        PI_AZURE_STORAGE_ACCOUNTNAME=101
        PI_AZURE_STORAGE_PROTOCOL=102
        PI_AZURE_STORAGE_ACCOUNTKEY=103
        PI_AZURE_STORAGE_CONTAINER=104
        PI_AZURE_STORAGE_FILENAME=105
    class Mysql:
        PI_MYSQL_DB_IP=201
        PI_MYSQL_DB_PORT=202
        PI_MYSQL_DB_NAME=203
        PI_MYSQL_DB_USER=204
        PI_MYSQL_DB_PASSWORD=205
        PI_MYSQL_TABLE_NAME=206
        PI_MYSQL_COLUMN_LIST=207

class CustomString:
    def __init__(self):
        self.__STRING_UNDERLINE=('_')
        self.__STRING_COMMA=(',')
        self.__STRING_SLASH=('/')
        self.__STRING_DASH=('-')
        self.__STRING_TAB=('\t')
        self.__STRING_SOH=('\x01')
        self.__STRING_STX=('\x03')
        self.__STRING_ETX=('\x03')
        self.__STRING_EOT=('\x04')
        self.__STRING_LF=('\n')
        self.__STRING_CR=('\r')
    def underline(self): 
        """ Get the string with char '_'. """
        return self.__STRING_UNDERLINE
    def comma(self): 
        """ Get the string with char ','. """
        return self.__STRING_COMMA
    def slash(self): 
        """ Get the string with char '/'. """
        return self.__STRING_SLASH
    def dash(self): 
        """ Get the string with char '-'. """
        return self.__STRING_DASH
    def tab(self): 
        """ Get the string with char '\t'. """
        return self.__STRING_TAB
    def soh(self): 
        """ Get the string with char '\x01'. """
        return self.__STRING_SOH
    def stx(self): 
        """ Get the string with char '\x02'. """
        return self.__STRING_STX
    def etx(self): 
        """ Get the string with char '\x03'. """
        return self.__STRING_ETX
    def eot(self): 
        """ Get the string with char '\x04'. """
        return self.__STRING_EOT
    def lf(self): 
        """ Get the string with char '\n'. """
        return self.__STRING_LF
    def cr(self): 
        """ Get the string with char '\r'. """
        return self.__STRING_CR