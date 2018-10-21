import os
import datetime
import requests

from flask import Flask
from flask import request
from flask import jsonify
from flask import make_response
from flask import redirect
from flask import url_for
from werkzeug import secure_filename

from InitModel import InitModel,timer,log as logging
from CustomDataBase import MySqlDataBase
from ShareData import CustomString #,PtternIndex


UPLOAD_FOLDER = '/data/ReportData/'
ALLOWED_EXTENSIONS = set(['txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'])

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

ip_allow_list=['10.78.22.21','10.78.22.14','220.134.230.152']
mysql_conf=None
azurestorage_conf=None
@app.errorhandler(404)
def not_found(error):
    #hyperspace.southeastasia.cloudapp.azure.com
    return make_response(jsonify({'error': 'Not found'}), 404)


@app.route('/staging/api/v1.0/login', methods=['POST'])
def staging_login():
    try:
        data=None
        try:
            data = request.json
        except:
            return make_response(jsonify({'request data error': 'request send data is not json format'}), 404)
        
        if ((not 'UserName' in request.json) or (not 'Password' in request.json)):
            return make_response(jsonify({'request data error': "request send data not include 'UserName' or 'Password' "}), 404)
        username=data['UserName']
        password=data['Password']
        out=''
        if ((username == 'hyperspace_root') and (password == 'hyperspace#root1017')):
            out='{0} allow login'.format(username)
        else:
            out='{0} not allow login, username/password not match in system.'.format(username)
        return out , 201
    except:
        return make_response(jsonify({'error': ''}), 404)


@app.route('/staging/api/v1.0/get_firms', methods=['POST'])
def staging_getdata():
    try:
        data=None
        try:
            data = request.json
        except:
            return make_response(jsonify({'request data error': 'request send data is not json format'}), 404)
            
        if ((not 'StartDate' in request.json) or (not 'EndDate' in request.json) or (not 'Confidence' in request.json)):
            return make_response(jsonify({'request data error': "request send data not include 'StartDate' or 'EndDate' or 'Confidence' "}), 404)
        sDate=data['StartDate']
        eDate=data['EndDate']
        confidence=data['Confidence']
        db=MySqlDataBase(mysql_conf)
        isConnectDB=False
        retryCount=0 
        retryMax=5
        delaySec=1
        while (not isConnectDB):
            isConnectDB=db.connectDB()
            if (isConnectDB): break
            if (retryCount>=retryMax): break
            retryCount+=1
            time.sleep(delaySec)
        if (not isConnectDB): logging.error('Connect DB is error, retry %d times, every time delay %d seconds.\n%s' % (retryMax,delaySec,db.getErrorMsg()))
        if (retryCount >0): 
            logging.warn('Connect DB is success, but retry %d times, every time delay %d seconds.\n%s' % (retryMax,delaySec,db.getErrorMsg()))
        else:
            logging.info('%s took %s.'  % ('Connect DB', timer.getSepndTime()) )
        timer.setTimeFlag()
        if (not db.get_tw_data(StartDate, EndDate, Confidence)): 
            logging.error('Querry FIRMS Tw Data is error.\n%s' % (db.getErrorMsg()))
        logging.info('%s took %s.'  % ('Querry data', timer.getSepndTime()) )
        
        results=db.getResult()
        logging.info('%s took %s.'  % ('Get ResultObject', timer.getSepndTime()) )
        out=''
        rowIndex=0
        totalRow=0
        column_names = [i[0] for i in results.description]
        try:
            for row in results:  # add table rows
                if (out != ''): out+= s.comma()
                out+=db.getRowToJsonString(row, column_names)
                totalRow+=1
            out='['+out+']'
            print(out)
            logging.info('Totla row = {0}'.format(totalRow))
            logging.info('%s took %s.'  % ('Output Data ', timer.getSepndTime()) )
        except Exception as e:
            err=str(type(e))+s.lf()+str(e)
            logging.error('Output Data Error:\n%s' % (err))
        db.close()
        if (out is None):
            return make_response(jsonify({'error': 'Get Data Error'}), 404)
        else:
            return out , 201
    except Exception as e:
        print(e)
        return make_response(jsonify({'error': ''}), 404)

@app.route('/staging/api/v1.0/get_near_real_time', methods=['POST'])
def staging_get_near_real_time():
    try:
        db=MySqlDataBase(mysql_conf)
        isConnectDB=False
        retryCount=0 
        retryMax=5
        delaySec=1
        while (not isConnectDB):
            isConnectDB=db.connectDB()
            if (isConnectDB): break
            if (retryCount>=retryMax): break
            retryCount+=1
            time.sleep(delaySec)
        if (not isConnectDB): logging.error('Connect DB is error, retry %d times, every time delay %d seconds.\n%s' % (retryMax,delaySec,db.getErrorMsg()))
        
        if (retryCount >0): 
            logging.warn('Connect DB is success, but retry %d times, every time delay %d seconds.\n%s' % (retryMax,delaySec,db.getErrorMsg()))
        else:
            logging.info('%s took %s.'  % ('Connect DB', timer.getSepndTime()) )
        timer.setTimeFlag()
        
        if (not db.get_near_real_time()): 
            logging.error('Querry FIRMS Tw Data is error.\n%s' % (db.getErrorMsg()))
        logging.info('%s took %s.'  % ('Querry data', timer.getSepndTime()) )
        results=db.getResult()
        logging.info('%s took %s.'  % ('Get ResultObject', timer.getSepndTime()) )
        out=''
        rowIndex=0
        totalRow=0
        column_names = [i[0] for i in results.description]
        try:
            for row in results:  # add table rows
                if (out != ''): out+= s.comma()
                out+=db.getRowToJsonString(row, column_names)
                totalRow+=1
            out='['+out+']'
            logging.info('Totla row = {0}'.format(totalRow))
            logging.info('%s took %s.'  % ('Output Data ', timer.getSepndTime()) )
        except Exception as e:
            err=str(type(e))+s.lf()+str(e)
            logging.error('Output Data Error:\n%s' % (err))
        if (out is None):
            return make_response(jsonify({'error': 'Get Data Error'}), 404)
        else:
            return out , 201
    except Exception as e:
        return make_response(jsonify({'error': ''}), 404)

@app.route('/staging/api/v1.0/report', methods=['POST'], strict_slashes=False)
def uploadFile():
    try:
        format = "%Y-%m-%dT%H:%M:%S"
        now = datetime.datetime.utcnow().strftime(format)
        request_send_keys=['UserName', 'Latitude', 'Longitude', 'Date']
        if 'File' not in request.files:
            return make_response(jsonify({'error': 'request send data no send key \'{key}\''.format(key='File')}), 201)
        for key in request_send_keys:
            if key not in request.form:
                return make_response(jsonify({'error': 'request send data no send key \'{key}\''.format(key=key)}), 201)
        file=request.files['File']
        #file = open('./root_test.jpg', 'rb').read()
        res = requests.post(url='https://southcentralus.api.cognitive.microsoft.com/customvision/v2.0/Prediction/<key>/image?iterationId=<key>',
                        data=file,headers={'Content-Type': 'application/octet-stream','Prediction-Key':'477f332a400b4be8a89ed0ec0bd62735'})
        data=res.json()
        return jsonify({'predictions': data['predictions'],'get_point':30}) , 201
    except Exception as e:
        print(e)
        return make_response(jsonify({'error': ''}), 404)

@app.route('/')
def index():
    client_ipaddr=request.remote_addr
    if (client_ipaddr in ip_allow_list) :
        if (client_ipaddr == ip_allow_list[0]) :
            return "Hello, GitLab!"
        elif (client_ipaddr == ip_allow_list[1]) :
            return "Hello, HL!"
        elif (client_ipaddr == ip_allow_list[2]) :
            return "Hello, Developer!"
    else:
        return make_response(jsonify({'error': 'Not found'}), 404)




if __name__ == '__main__':
    confPath=''
    
    confPath='/home/shianglun/HyperspaceWebService/Conf/Hyperspace.conf'

#     if (len(sys.argv)!=4):
#         print_error_exit('main.py <confPath>')
#     else:
#         confPath=sys.argv[1]
    StartDate='2018-10-01'
    EndDate='2018-10-17'
    Confidence=30
        
    s=CustomString()
    timer=timer()
    initM=InitModel()
    initM.setConf(confPath)
    if (initM.isLogConfPathNone()): print_error_exit('Log conf path is undefined.')
    
    global logging
    logging=logging(initM.getLogConfPath(), 'HyperspaceWebService')
    logging.info('%s took %s.'  % ('Set conf', timer.getSepndTime()) )
    
    if (initM.isMySQLConfNone()): 
        logging.error('%s in confFile(%s) is undefined.'  % ('MySQL conf',confPath))
    if (initM.isMySQLConfHasNull()): 
        logging.error('%s in confFile(%s) is incomplete definition.\n%s'  % ('MySQL conf',confPath,initM.getOracleConf().toString()))
    #global mysql_conf
    mysql_conf=initM.getMySQLConf()
    
    if (initM.isAzureStorageConfNone()): 
        logging.error('%s in confFile(%s) is undefined.'  % ('AzureStorage conf',confPath))
    if (initM.isAzureStorageConfHasNull()): 
        logging.error('%s in confFile(%s) is incomplete definition.\n%s'  % ('AzureStorage conf',confPath,initM.getOracleConf().toString()))
    #global azurestorage_conf
    azurestorage_conf=initM.getAzureStorageConf()
    
    
    app.run(host='0.0.0.0', port=80, debug=True)

