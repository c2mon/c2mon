'''
    Library for the deploy tool.
    
    Contains functions for various file operations and installation routines.
    
    F.Ehm, CERN 2011  
'''
import logging 

import os
import shutil
import socket
import tempfile
import time
import tarfile
import commands
import string
import smtplib
import glob
from re import match


# Used to keep the name of the temporary logfile, so we can copy it later to the installation directory
#
tmpinstallLogFile = None


#
#
simulationMode = False

log = logging.getLogger('tools')

def preserveFile( orig, new):
    log = logging.getLogger("install")
    log.debug('Preserving %s to %s' %(orig,new))

    if not os.path.exists(orig):
        log.warning("Can't preserve file '%s'. Previous instance is missing in '%s'" %(new, orig))
        return

    if os.path.exists(new) :
        log.warning('Removing existing %s due to preserve instructions.' %new)
        if os.path.isdir(new):
                shutil.rmtree(new)
        if os.path.isfile(new):
                os.remove(new)

    command='cp -al %s %s' %(orig, new)
    log.debug(command)

    result = commands.getstatusoutput(command)
    if result[0] != 0:
        raise Exception('While preserving file %s:' + str(result))


def createLink( dir, source, target ):
    log = logging.getLogger("install")

    c_dir = os.getcwd()
    os.chdir(dir)
    
    if os.path.islink(source):
        log.warning('The source you refer to for your symbolic link in %s is a symbolic link itself!' %(target))
    if os.path.exists(source):
        os.chdir(c_dir)
        log.error('%s already exist !' %source)
    if not os.path.exists(target):
        os.chdir(c_dir)
        log.error('%s does not exist !' %target)
    # do we have access to the target ?
    #checkRemove(target)

    os.symlink(target, source)
    os.chdir(c_dir)

def replaceStringInFile(fileName, search, replace ):
    try :
        content = open (fileName, 'r').read()
        content = content.replace( search, replace )
        open(fileName, 'w').write(content)
    except Exception,e :
        raise Exception("Cannot replace String '" + search + "' in '" + fileName + "' : "+ str(e[1]))



#
# reorganizes the backup directories
#
def makeBackup(installDir, nbBackups):
    global simulationMode
    
    log = logging.getLogger("install")
    if nbBackups == 0 :
        log.info('No backup requested.')
        return  # we do nothing
    log.info('Creating backup for %s (max %s are kept) ' %(installDir,str(nbBackups)))

    if installDir.endswith('/'):
        installDir = installDir[:-1]

    # remove oldest backup dir
    log.debug('Rolling backup directories.')
    dir = installDir + '.'+ str(nbBackups)
    if os.path.exists(installDir + '.'+ str(nbBackups)):
        log.debug('Removing oldest backup')
        checkRemove(dir)
        if not simulationMode:
            shutil.rmtree(dir)

    # rename previous backup dirs (i.e. dir1 -> dir2, dir2 -> dir3
    for i in range ( nbBackups, 1, -1 ):
        newName = installDir + '.' + str(i-1)
        targetDir = installDir + '.' + str(i)
        if os.path.exists( newName ):
            checkRemove(newName)
            checkRemove(targetDir)
            if not simulationMode:
                shutil.move( newName ,  targetDir )

    # move latest to <dir>.1
    if os.path.exists( installDir ):
        checkRemove(dir)
        if not simulationMode:
            shutil.move( installDir, installDir + '.1' )
        return installDir + '.1'
    else:
        return None



def doPreInstall(app, installConfig):
    '''
       @param the Application to install
       @param the InstallConfig
       @return the installation path which has been created
    '''
    global tmpinstallLogFile
    global simulationMode
    
    # check if process is running
    for script in app.getStartScripts():
        if os.path.exists('/etc/transfer.ref') and isProcessInTransferRef(script.getProcessName()):
            logging.warn("Your process '%s' is NOT in the transfer.ref" %script.getProcessName())
            
    # set up the temp file for the logs.
    log = logging.getLogger("install")
    tmpinstallLogFile = tempfile.mkstemp()[1]
    hdlr = logging.FileHandler(filename=tmpinstallLogFile)
    formatter = logging.Formatter("[%(levelname)s] %(message)s")
    hdlr.setFormatter(formatter)
    log.addHandler(hdlr)
    
    # set the installation directory to the one desired by the user.
    if (installConfig.getInstallDir() != None):
        app.setInstallDir(installConfig.getInstallDir())

    rootDir = app.getDeploymentInfo().getDirectoryStructure()
    
    # Add default directories
    #
    for d in ['bin','log']:
        if rootDir.getDirectory(d) == None:
            log.debug("Adding empty Directory '%s' " %d)
            from common import Directory
            rootDir.addDirectory(Directory(d))

    
    installDir = app.getInstallDir()

    createInstallLogHeader(app, installConfig)

    log.info("Starting PreInstallation for " + app.getName() + " in " + installDir)
    checkRemove(installDir)
    if not installConfig.isForceMode() and not checkOpenFileDescriptors(installDir):
        raise Exception("I cannot do a backup as the existing directory is still used by a process. You can override this by the '-f' option.")
    previousInstall = makeBackup(installDir, app.getDeploymentInfo().getBackupPolicy().getKeepBackups())
    log.info("Creating " + installDir)
    if not simulationMode:
        mkdir(installDir)
    log.info("Preinstallation finished")
    return installDir
    

def doPostInstall(app, installConfig):
    global tmpinstallLogFile
    global simulationMode
    
    log = logging.getLogger("install")

    log.info("Postinstallation starts")

    installDir = app.getInstallDir()

    createDirectoryStructure(installDir, app.getDeploymentInfo().getDirectoryStructure())
    
    for script in app.getStartScripts():
        script.setAppVersion(app.getProduct().getRealVersion())
        
        if not simulationMode:
            mkdir(os.path.join(installDir, 'bin'))
        
        if script.getMain() != None:
            if not simulationMode :
                script.setInstallDir(installDir)
                loc = script.generateStartScript()
            targetLoc = os.path.join(installDir, 'bin', script.getProcessName())
                shutil.copy2(loc, targetLoc)
                os.chmod(targetLoc, 0755)
                log.debug("Copy startscript from %s to %s" %(loc, targetLoc))
    
    for file in app.getDeploymentInfo().getBackupPolicy().getPreserveFiles():
        log.debug("Preserving file " + file)
        if not simulationMode:
            preserveFile(os.path.join(installDir +".1", file), os.path.join(installDir,file))
        

    replaceList = app.getDeploymentInfo().getStringReplacements()
    for r in replaceList.keys():
    for target in replaceList[r]:
            log.debug("Replacing in %s '%s'=>'%s'" %(os.path.join(installDir,r), target[0], target[1]))
            if not simulationMode:
                replaceStringInFile(os.path.join(installDir,r), target[0], target[1])
    
    if app.getDeploymentInfo().getJarSignerConfig() != None:
        import jarsigner
        signer = jarsigner.getJarSignerFromconfig(app.getDeploymentInfo().getJarSignerConfig())
        
        log.info("Signing jars in 'lib/*.jar'")
        if not simulationMode:
            files = [n for n in os.listdir(os.path.join(installDir,'lib')) if match(".*\.jar", n)]
            files = [os.path.join(installDir,'lib',n) for n in files]
            signer.setFiles(files)
            signer.signJars()
    

    # read our install log to send the content
    infile = open(tmpinstallLogFile,"r")
    installLogText = ""
    for i in infile.readlines(): installLogText +=i
    infile.close()
    
    log.info("Postinstallation finished")
    # copy the log file to the installDir
    

    if installConfig.isSendMail() and app.getDeploymentInfo().getNotificationList() != None:
        emailList = [m.getAdress() for m in app.getDeploymentInfo().getNotificationList()]
        log.info("Sending notifications to %s"  %str(emailList))
        sendNotification(emailList, app, installLogText)
    jmx = readJmxInfo(os.path.join(app.getInstallDir(), 'bin'))
    if len(jmx.keys()) > 0:
        log.debug("Found jmx info :\n%s" %jmx)
        notifyCenter(socket.gethostname(), app.getInstallDir(), app.getProduct().getName(), app.getName(), findTriggeringUser(), 'install', app.getProduct().getRealVersion(), app.getProduct().getReleaseDate(), jmx)
    if not simulationMode:
        shutil.move(tmpinstallLogFile, os.path.join(installDir,"install.log"))
    
 
def createDirectoryStructure(installPath, dir):
    log = logging.getLogger("install")
    
    localdir = os.path.join(installPath, dir.getPath()) 
    log.debug("Creating Directory %s" %localdir)
    
    if not simulationMode:
        mkdir(localdir)
        os.chmod( localdir, string.atoi(dir.getMode(),8))
    
    # copy first from the sourceDir - if given
    if dir.getSourceDir() != None:
        # copy the files from the source folder.
        log.debug("Copying content of %s to %s" %(dir.getSourceDir(),localdir))
        if not simulationMode:
            ret = os.system("cp -pRL %s/* %s "%(dir.getSourceDir(), localdir))
            if ret > 0:
                raise Exception("Cannot copy content of %s to %s." %(dir.getSourceDir(), localdir))
        
    # then create the children directories
    for child in dir.getDirectories():
        createDirectoryStructure(installPath, child)


    # and finally copy files into here   
    files = sorted(dir.getFiles(), key=lambda file:file.getIsUnpackRequired())
    files.reverse() 
    for file in files: 
        fileName = file.getName()
        
        result = handleGetFile(file.getSource(), localdir, fileName)
        if file.getIsUnpackRequired():
            log.debug("Unpacking content of %s to %s" %(result,localdir))
            if not simulationMode:
               extractTar(result, localdir)
               #os.system("tar -C '%s' -xkzf '%s' " %(localdir, result))
        log.debug("Setting mask %s for %s" %(file.getMode(), result))
        if not simulationMode and result != None:
            os.chmod( result, string.atoi(file.getMode(),8))
            
    # of course, do  not forget the links 
    for link in dir.getLinks():
        target = os.path.join(localdir,link.getSourceParentDir().getPath())
        log.debug('Creating link in %s named %s pointing to %s' %(target, link.getSource(), link.getTarget()))
        if not simulationMode:
            createLink(target, link.getSource(), link.getTarget())
    #if not simulationMode:
    #    os.chmod( localdir, string.atoi(dir.getMode(),8))
 
    

def createInstallLogHeader(app, installConfig):
    log = logging.getLogger("install")
    info = log.info
    
    prod = app.getProduct()

    changeText = prod.getChangelog()
    if changeText != None and len(changeText) > 0:
        info(changeText)
    info('')
    info("-------------------------------------------------------")
    info("")
    info("Installation of application '%s' for product '%s'" %(app.getName(), app.getProduct().getName()) )
    info('')
    info('Application             : %s' %app.getName())
    info('Product                 : %s' %prod.getName())
    info('Version                 : %s (%s)' %(prod.getRealVersion(),prod.getAliasVersion()))
    info('Release Date            : %s' %prod.getReleaseDate())
    info('Source                  : %s' %prod.getSourceUrl())
    info('Started on              : %s' %(time.strftime("%a, %d %b %Y %H:%M:%S ", time.localtime())))
    info('Target directory is     : %s' %app.getInstallDir())  # we could take this also from installConfig. They're the same
    info('Triggered by            : %s' %str(findTriggeringUser()))
    info('Host                    : %s' %socket.gethostname())
    info('Force installaton is    : %s' %installConfig.isForceMode())
    info('Automatic rollback      : False ')
    info('Keeping backups         : %s' %app.getDeploymentInfo().getBackupPolicy().getKeepBackups())
    info('SimulationMode          : %s' %installConfig.isSimulate())
    info('')
    log.debug('Install log :')





def sendNotification(elements, application, body):
    from email.MIMEText import MIMEText
    if elements == None or len(elements) == 0:
        return

    hostname = socket.gethostname()
    sender = 'copera@%s' %(hostname)
    msg = MIMEText(body)
    msg['Subject'] = '[DEPLOYMENT] %s has been deployed on %s ' %(application.getName(), hostname)
    msg['From'] = sender
    msg['To'] =  ', '.join(elements)
    s = smtplib.SMTP()
    s.connect()
    s.sendmail(sender, elements , msg.as_string())
    s.close()



def notifyCenter(host, installPath, product, application, user, installMode, version, releaseDate, otherProps={}, body = ""):
    ''' Sends a deployment message via STOMP to the deployment management center '''
    try :
        logging.debug("Attempting to send deployment notification ...")
        mysocket = socket.socket( socket.AF_INET, socket.SOCK_STREAM)
        mysocket.settimeout(2)
        mysocket.connect( ("jms-co-dev", 61680) )

        mysocket.sendall("CONNECT\n\n\x00")
        frame = mysocket.recv(1024)
        if frame == None :
                raise Exception("Recieved unknown command on connection")
        if frame.split('\n')[0] == 'CONNECTED':
                logging.debug("Ok. connected...")
        else:
                raise Exception("Didn't receive 'CONNECTED' frame")

        otherPropText = ""
        if otherProps != None and len(otherProps) > 0:
        for a in otherProps.keys():
        otherPropText += "%s:%s\n" %(a, otherProps[a])

        expires = time.time() + 20
        expires = long(expires) * 1000
        text =  '''SEND\n\
destination:/queue/CERN.DEPLOYMENT\n\
HOST:%s\n\
INSTALLPATH:%s\n\
PRODUCT:%s\n\
APPLICATION:%s\n\
USER:%s\n\
INSTALLMODE:%s\n\
VERSION:%s\n\
RELEASEDATE:%s\n\
TIME:%s\n\
persistent:false\n\
expires:%s\n%s\n\n%s\n\n\x00''' %(host, installPath, product, application, user, installMode, version, releaseDate, long(time.time()) * 1000, 0, otherPropText,body)

        logging.debug("Sending frame : \n%s" %text)
        mysocket.sendall(text)
        mysocket.sendall("DISCONNECT\x00")
        mysocket.close()


    except Exception, e:
        logging.warning("Cannot send deployment notification :  " + str(e))
    notifyCenterTracing(host, installPath, product, application, user, installMode, version, releaseDate, otherProps)
    logging.debug("Deployment notification sent.")



def notifyCenterTracing(host, installPath, product, application, user, installMode, version, releaseDate, otherProps):
    '''
    Sends a message to the tracing system.
    '''
    host = socket.gethostname()
    server = 'feedback-cfg-pro'
    port = '6205'

        message = '''SEND\n\
destination:/queue/CERN.DEPLOYMENT\n\
TS:%s\n\
PID:%s\n\
PROCESSNAME:%s\n\
DOMAIN:DEPLOYMENT
SUBDOMAIN:deploy-tool
SOURCE:%s
VERSION:1.0.1
MESSAGETYPE:CONFIG
HOST:%s\n\n''' %(long(time.time()) , os.getpid(), __file__ , product, host)

    message += 'product=%s;application=%s;version=%s;installPath=%s;user=%s;installMode=%s;releaseDate=%s||Installation of %s' %(product,application,version,installPath,user,installMode,releaseDate,application)
        for a in otherProps.keys():
                message += ";%s=%s" %(a, otherProps[a])
    
        message +='''\n\x00'''

        logging.debug("Sending tracing message to %s:%s  \n%s" %(server, port,message))
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        sock.sendto(message, (server, int(port)))
        sock.close()



def readJmxInfo(installPath):
    port = None
    secure = False
    
    if not os.path.exists(installPath): return {}
    
    for f in os.listdir(installPath):
    path = os.path.join(installPath,f)

        if os.access(path, os.X_OK) and os.path.isfile(path):
        c = open(path, "r").read()
        try:
        pos1 = c.rindex("com.sun.management.jmxremote.port=")
        if pos1 > 0:
            pos2 = c.index(" ", pos1)
            port = c[pos1+34:pos2].replace("\"","")
        if c.index("com.sun.management.jmxremote.password.file=") > 0 and c.index("-Dcom.sun.management.jmxremote.authenticate=true") > 0:
            secure = True
        except ValueError, e:
        pass
        # IGNORE
    if port != None:
        return {"MPORT" : port,  "MSECURED" : secure}
    else:
        return {}





def readChangelog(fileName):
    ''' Reads the changelog file '''
    text = ''
    sep = 0
    try:
        logging.debug("Extracting changelog information from " + fileName)
        infile = open(fileName,"r")
        content = infile.readlines()
        for i in range(0, len(content)):
            li = content[i].strip()
            #for line in content:
            if len(li) == 0:
                sep += 1
            if sep == 2:
                break
            text += li +"\n"
        infile.close()

    except Exception, e:
        logging.warning ("Can't read from " + fileName + " : " + str(e))
        text = ''

    return text




def findPreviousInstalledVersion(installDir):
    try:
        if not os.path.exists(installDir):
            # do nothing
            return None

        log = open(os.path.join(installDir, "install.log"), 'r')

        # reading the log file and find the Version : xx entry
        for line in log:
            line = line.split(":")
            if line[0].strip() == "Version":
                version = line[1].rstrip('\n').strip().split(' ')
                if len(version) > 1:
                    ret = version[1]
                    ret = ret[1:len(ret)-1]
                    logging.debug("previous version info found : " + ret)
                    return ret
    except Exception, e:
        logging.warning("Unable to find previous installation. " + str(e))
    return None

def isProcessInTransferRef(procName):
    
    infile = open("/etc/transfer.ref","r")
    text = infile.read()
    infile.close()
    return ((text.find(procName) > 0) == True)

def isProcRunning(procName):
    '''checks if the passed process is already running - only linux'''
    output = commands.getoutput("ps -f | awk '{print $8}' |grep -i " + procName + " | grep -v 'grep \|ps \|python'")
    logging.debug("checkForRunningProc : " + procName)
    return len(output) > 0

def findTriggeringUser():
    '''
    Evaluates the real user if logged in as an fictive account (
    '''
    if 'USER' in os.environ:
        user = os.environ['USER']
    elif 'LOGNAME' in os.environ:
        user = os.environ['LOGNAME']
    else:
        user = str(os.getuid())
        if user == 50624:
            user = 'copera'
        
    if user == 'copera':
        if 'ORIG_USER' in os.environ:
            orig_user = os.environ['ORIG_USER']
        else:
            raise Exception ("I can't determine your *original* login name. It seems that you've logged onto this machine using the copera account and not as yourself. To fix this do the following:\n 1.log out\n 2.login as yourself\n 3.execute 'super copera'\n 4.Repeat deploy command ")
    else:
        orig_user = user
    return (orig_user, user)


def mkdir(dir):
    try:
        os.makedirs(dir)
    except OSError, e:
        if e[0] != 17:
            raise


def readXmlFromFile(configFile):
    '''
    Reads the XML config file
    '''
    checkNotEmpty(configFile, 'No configuration file (product.xml) given.')
    import platform
    pythonVersion = platform.python_version() 
    
    if pythonVersion == "2.4.3":
        from xml.dom.ext.reader import Sax2
        reader = Sax2.Reader()
        return reader.fromUri(configFile)
    elif pythonVersion == "2.6.6":
        from xml.dom.minidom import parse
        return parse(configFile)
    else:
        import libxml2
        return libxml2.parseFile(configFile)
    


def readXmlFromString(xmlText):
    import platform
    pythonVersion = platform.python_version() 
    
    import libxml2
    return libxml2.parseDoc(xmlText)
    

def checkNotEmpty(value, errorMsg):
    if value == None:
        raise Exception(errorMsg)
def checkIsValidObject(value, object, errorMsg):
    if not isinstance(value, object):
        raise Exception(errorMsg)
    
def checkRemove(dir):
    if os.path.exists(dir) and not os.access(dir, os.W_OK):
        raise Exception ("I don't have permissions for " + dir)
def checkMove(dir):
    checkRemove(dir)
    
def checkOpenFileDescriptors(path):
    '''
    '''
    checkRemove(path)
    return os.system("/usr/sbin/lsof | grep %s  | grep -v cwd" %path)
    

def getFileFromTar(targz, fileName, targetDir):
    tar = tarfile.open(targz, mode='r:gz')
    tar.extract(fileName, path=targetDir)
    tar.close()
    return targetDir + os.path.sep + fileName

def setDefaultIfNull(var, defaultValue):
        if var == None or len(var) == 0:
                return defaultValue
        return var


def handleWgetGet(url, local, localFileName):
    '''
    Fetches the a file from the given url and stores it local under file
    @param url: the url to fetch the file from 
    @param local: the local directory where to store the file
    @param localFileName: the local fileName (same as url if not given)
    @return the local path of the downloaded file

    '''
    localPath = local + os.path.sep + localFileName
    log.debug("wget " + url + " -O " + localPath)
    errlog = tempfile.mkstemp()
    ret = os.system("/usr/bin/wget \"%s\" -O %s -o %s" %(url,localPath,errlog[1]))
    if ret > 0:
        content = ""
        infile = open(errlog[1], "r")
        for l in infile.readlines():
                content += " [WGET]" + l
        os.remove(localPath)
        os.remove(errlog[1])
        raise Exception("Error downloading from " + url + " :\n" + content)
    return localPath

def extractTar(file, target):
    import tarfile
    '''
    @param file: The file to extract using tar
    @param target : the directory where this file should be extracted to. 
    '''
    mylog = logging.getLogger('install')
    path = os.getcwd()
    mylog.info("Extracting %s to %s" %(file, target))
    if not simulationMode:
        t = tarfile.open(file, 'r|gz')
        t.extractall(target)
        #os.chdir(target)
        #ret = os.system("tar -xzf " + file)
        #os.chdir(path)
        #if ret > 0:
        #    raise Exception()



def handleGetFile(sourceUrl, targetDir, localFileName=None):

    if sourceUrl.startswith("http://"):
        if localFileName == None:
            localFileName = sourceUrl[(s.rfind('/')+1):]
        return handleWgetGet(sourceUrl, targetDir, localFileName)
    elif sourceUrl.startswith("svn"):
        return handleSvnGet(sourceUrl, targetDir, localFileName)
    else:
        return handleCopyGet(sourceUrl, targetDir, localFileName)


def handleCopyGet(source, targetLoc, localFileName=None):
    global simulationMode
    log = logging.getLogger("install")
    
    if source.rfind('*') > 0 or source.rfind('[') > 0:
        for n in glob.glob(source):
            fileName = os.path.basename(n)
            log.debug("Copying file from REGEXP : %s -> %s" %(n, os.path.join(targetLoc,fileName)))
            if not simulationMode:
                shutil.copy2(n, os.path.join(targetLoc,fileName))
        return None
    else:
        if localFileName != None:
            targetLoc = os.path.join(targetLoc, localFileName)
        log.debug("Copying file from %s -> %s" %(source,targetLoc))
        if not simulationMode:
            shutil.copy2(source, targetLoc)
        return targetLoc
    
    
def handleSvnGet(source, targetLoc, localFileName=None):
    global simulationMode
    log = logging.getLogger("install")
 
    if localFileName == None:
        log.debug("Getting from SVN %s -> %s" %(source,targetLoc))
        if not simulationMode:
            ret = os.system("svn cat '%s' 1>&2 > %s" %(source, targetLoc))
    else:
        targetLoc = os.path.join(targetLoc, localFileName)
        log.debug("Getting from SVN %s -> %s" %(source,targetLoc))
        if not simulationMode:
            ret = os.system("svn cat " + source + ">" + targetLoc)
    
    if not simulationMode:
        if ret > 0:
            raise Exception("Error downloading from " + source )
    
    return targetLoc
     


def getDepricatedInfo(msg):
    return "\n\n========== DEPRICATED USAGE DETECTED =========\n" + \
        "|  " + msg + "\n" + \
        "|\n" + \
        "==============================================\n\n"
           



def red(string):
    return '\033[1;31m%s\033[1;m' %string
def darkred(string):
    return '\033[1;38m%s\033[1;m' %string
def yellow(string):
    return '\033[1;33m%s\033[1;m' %string
def green(string):
    return '\033[1;32m%s\033[1;m' %string
    

