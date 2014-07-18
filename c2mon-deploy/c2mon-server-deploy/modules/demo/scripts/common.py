'''
    Library for the deploy tool.
    
    Contains objects for the deploy tool. 
    
    F.Ehm, CERN 2011  
'''

import copy
import tools
import os
import re

import socket
import tempfile
from tools import checkNotEmpty


class Product(object):
    def __init__(self, name=None, sourceUrl=None, aliasVersion='PRO', realVersion=None, releaseDate=None):
            self.__name = name
            self.setVersion(aliasVersion, realVersion)
            self.setReleaseDate(releaseDate)
            self.__sourceUrl = sourceUrl
            self.__applications = {}
            self.__changelog = None
            self.__deployConfigFile = None
            
    def setVersion(self, aliasVersion, realVersion):
            self.__aliasVersion = aliasVersion
            self.__realVersion = realVersion
    def setReleaseDate(self, releaseDate):
        self.__releaseDate = releaseDate
    def getReleaseDate(self):
            return self.__releaseDate
    def getRealVersion(self):
            return self.__realVersion
    def getAliasVersion(self):
            return self.__aliasVersion
    def getName(self):
            return self.__name
    def addApplication(self, newApp):
            self.__applications[newApp.getName()] = newApp
    def getApplication(self, app):
            return self.__applications[app]
    def hasApplication(self,appName):
        return self.__applications.has_key(appName)
    def removeApplication(self, appName):
        self.__applications.pop(appName)
    def getApplications(self):
        return self.__applications.values()
    def getSourceUrl(self):
        return self.__sourceUrl
    def setSourceUrl(self, url):
        self.__sourceUrl = url
    def setChangeLog(self, txt):
        self.__changelog = txt
    def setDeployConfig(self, deployConfig):
        self.__deployConfigFile = deployConfig
    def getDeployConfig(self):
        return self.__deployConfigFile
    def getChangelog(self):
        return self.__changelog

    def __repr__(self):
        ret = "Product {name=%s,version=%s,alias=%s,releaseDate=%s,sourceUrl=%s\n" %(self.__name,self.__realVersion,self.__aliasVersion,self.__aliasVersion, self.__sourceUrl)
        for a in self.__applications.values():
                ret += str(a) +"\n"
        return ret + '}'


class InstallConfig(object):

    def __init__(self):
        self.__host = socket.gethostname()
        self.__user = tools.findTriggeringUser()
        self.__isSendMail = True
        self.__version = None
        self.__forceMode = False
        self.__simulate = False
        self.__productName = None
        self.__applicationName = None
        self.__installDir = None
        self.__sourceUrl = None
        self.__deployConfig = None
        self.__maven = False
        self.__rollback = False
    self.__isUpdate = False
    self.__showDiff = False
        

    def setSendMail(self, flag):
        self.__isSendMail = flag
    def isSendMail(self):
        return self.__isSendMail
    def setForceMode(self, flag):
        self.__forceMode = flag
    def isForceMode(self):
        return self.__forceMode
    def setSimulate(self, flag):
        self.__simulate = flag
        if flag == True:
            self.setSendMail(False)
        else:
            self.setSendMail(True)
    def isSimulate(self):
        return self.__simulate
    def setVersion(self, version):
        self.__version = version
    def getVersion(self):
        return self.__version
    def setProductName(self, prod):
        self.__productName = prod
    def getProductName(self):
        return self.__productName
    def setApplicationName(self, app):
        self.__applicationName = app
    def getApplicationName(self):
        return self.__applicationName
    def setInstallDir(self, dir):
        self.__installDir = dir
    def getInstallDir(self):
        return self.__installDir
    def isMaven(self):
        return self.__maven
    def setIsMaven(self, flag):
        self.__maven = flag
    def getSourceUrl(self):
        return self.__sourceUrl
    def setSourceUrl(self, url):
        self.__sourceUrl = url
    def setDeployConfig(self, configFile):
        self.__deployConfig = configFile
    def getDeployConfig(self):
        return self.__deployConfig
    def getTriggeringUser(self):
        return self.__user
    def setRollback(self, val):
        self.__rollback = val
    def isRollback(self):
        return self.__rollback
    def getHost(self):
        return self.__host
    def getIsUpdate(self):
        return self.__isUpdate
    def setIsUpdate(self, flag):
        self.__isUpdate = flag
    def setShowDiff(self, flag):
        self.__showDiff = flag
    def isShowDiff(self):
        return self.__showDiff
    def __str__(self):
        return "InstallConfig {ProductName=%s, ApplicationName=%s, Version=%s, SourceUrl=%s, Force=%s, isMaven=%s, InstallDir=%s, User=%s, Host=%s, Rollback=%s, Update=%s}" % (self.__productName, self.__applicationName, self.__version, self.__sourceUrl, self.__forceMode, self.__maven, self.__installDir, self.__user, self.__host, self.__rollback, self.__isUpdate)
    def check(self):
        tools.checkNotEmpty(self.__productName, 'ProductName not valid')
        tools.checkNotEmpty(self.__application, 'Application not given')



class Application(object):

        def __init__(self, product, name, installDir=None, deploymentInfo=None):
            self.__product = product
            self.__name = name
            
            if installDir == None:
                self.__installDir = '/opt/' + product.getName() + os.path.sep + self.__name
            else:
                self.setInstallDir(installDir)
            if deploymentInfo != None:
                self.setDeploymentInfo(deploymentInfo)
            else:
                self.__deploymentInfo = DeploymentInfo()
            self.__startscripts = {}

        def setInstallDir(self, installDir):
            self.__installDir = installDir
        def setDeploymentInfo(self, deploymentInfo):
            self.__deploymentInfo = deploymentInfo
        def getDeploymentInfo(self):
            return self.__deploymentInfo
        def getProduct(self):
            return self.__product
        def getName(self):
            return self.__name
        def setName(self,name):
            self.__name = name
        def getInstallDir(self):
            return self.__installDir
        def removeStartScripts(self):
            self.__startscripts = {}
        def removeStartScript(self, processName):
            if self.getStartScript(processName) == None : return
            self.startscripts.pop(processName)
        def addStartScript(self, script):
            self.__startscripts[script.getProcessName()] = script
        def getStartScripts(self):
            return self.__startscripts.values()
        def getStartScript(self, procName):
            if self.__startscripts.has_key(procName):
                return self.__startscripts[procName]
            else:
                return None
        def removeStartScript(self, procName):
            if self.__startscripts.has_key(procName):
                return self.__startscripts.pop(procName)
            else:
                return None
        def getCopy(self):
            ret = Application(self.getProduct(), self.getName(), self.getInstallDir(), self.getDeploymentInfo().getCopy())
            for s in self.__startscripts.values():
                ret.addStartScript(s.getCopy())
            return ret
        def __str__(self):
            res = "Application {ProductName=" + self.__product.getName() + ",ApplicationName=" + str(self.__name) + ",InstallDir=" + str(self.__installDir) + "\n" 
            if self.__deploymentInfo != None:
                res += str(self.__deploymentInfo)
            else:
                res += "No deployment info"
            res += "\n"
            for i in self.__startscripts.keys():
                s = self.getStartScript(i)
                res += str(i) + " " + str(s) + "\n"
            return res
        
class WebApp(Product):
        '''
           Represents an product which runs in a web server (tomcat, jetty).
        '''
    
        def __init__(self, product, name, installDir, deploymentInfo):
            super(Application, self).__init__(product, name, installDir, deploymentInfo)

        def getContainerType(self):
            return self.__containerType
        def setContainerType(self, webcontainer):
        self.__containerType = webcontainer
        


class DeploymentInfo(object):
        def __init__(self):
            self.__backupPolicy = BackupPolicy()
            self.__directories = {}
            self.__stringReplacement = {}
            self.__notificationList = []
            self.__jarsigner = None
            self.__installLocation = None
            
            # create default root directory structure
            self.setDirectoryStructure(Directory("root"))
        def setBackupPolicy(self, bkpPolicy):
            checkNotEmpty(bkpPolicy,'Passed BackupPolicy is None')
            if isinstance(bkpPolicy, BackupPolicy) == False:
                raise Exception("Passed object is not a BackupPolicy.")
            self.__backupPolicy = bkpPolicy
        def getBackupPolicy(self):
            return self.__backupPolicy
        def setDirectoryStructure(self, rootDir):
            if isinstance(rootDir, Directory) == False:
                raise Exception("Passed object is not a Directory.")
            self.__directories["root"] = rootDir
        def getDirectoryStructure(self):
            return self.__directories["root"]
        def addNotificationList(self, notList):
            self.__notificationList = self.__notificationList + notList
        def getNotificationList(self):
            return self.__notificationList
        def setStringReplacements(self, replacementList):
            self.__stringReplacement = replacementList
        def getStringReplacements(self):
            return self.__stringReplacement
        def setInstallLocation(self, path):
            self.__installLocation = path
        def getInstallLocation(self):
            return self.__installLocation
        def __str__(self):
            ret = 'DeploymentInfo {directories={\n'
            for i in self.__directories.values(): ret += "   "+ str(i) + "\n"
            ret += "  }notificationList=%s,backup=%s\n}" % (str(self.__notificationList), str(self.__backupPolicy))
            return ret
            
        def getJarSignerConfig(self):
            return self.__jarsigner
        def setJarSignerConfig(self, jarsigner):
            self.__jarsigner = jarsigner
        def getCopy(self):
            return copy.deepcopy(self)


class Notification(object):

        def __init__(self, mail, sendAtRestart=False):
            self.__adress = mail
            self.__sendAtRestart = sendAtRestart
        def getAdress(self):
            return self.__adress
        def setSendAtRestart(self, flag):
            if flag != None:
                self.__sendAtRestart = flag
        def getSendAtRestart(self):
            return self.__sendAtRestart
        def __str__(self):
            return "Notification {mail=%s,sendAtRestart=%s}" %(str(self.getAdress()), str(self.getSendAtRestart()))
        def getCopy(self):
            return copy.deepcopy(self)
    

class BackupPolicy(object):
        
        def __init__(self):
            self.__keepBackups = 1
            self.__preserveFiles = []
    
        def setKeepBackups(self, newNumber):
            self.__keepBackups = newNumber
            self.__preserveFiles = []
        def getKeepBackups(self):
            return self.__keepBackups
        def addPreserveFile(self, source):
            self.__preserveFiles.append(source)
        def addPreserveDir(self, source):
            self.__preserveFiles.append(source)
        def getPreserveFiles(self):
            return self.__preserveFiles
        def __str__(self):
            return "Backup Policy {keepBackups=" + str(self.__keepBackups) + ", toPreserve=" + str(self.__preserveFiles) + "}"
        def getCopy(self):
            return copy.deepcopy(self)



class SimpleFile(object):
        ''' Simple file output '''
        def __init__(self,fileName):
                self.fileName = fileName
        def __str__(self):
                return '2>&1 >> %s' %(self.fileName)


class LogRotateFile(object):
        ''' Represents a file that is log logrotated
        '''
        __fileName = None
        __maxSize = "10M"
        __format = '%Y%m%d%H%M'
        __keepMaxFiles = 10

        def __init__(self, fileName):
            if fileName == None or fileName.__class__ != str:
                raise Exception('Invalid logrotate file name : ' + str(fileName))
            self.__fileName = fileName
        def getFileName(self):
            return self.__fileName
        def setFileName(self, val):
            checkNotEmpty(val,'Passed file name is None')
            self.__fileName = val
        def setMaxFileSize(self, val):
            if not re.match("\d{1,4}[M|m]", val):
                raise Exception("Passed value '%s' is not valid for maxFileSize")
            self.__maxSize = val
        def getMaxFileSize(self):
            return self.__maxSize
        def setKeepMaxFiles(self, val):
            int(val)
            self.__keepMaxFiles = val
        def getKeepMaxFiles(self):
            return self.__keepMaxFiles
        def __str__(self):
            return "2>&1 | /usr/sbin/rotatelogs %s%s %s" % (self.__fileName, self.__format, self.__maxSize)




class Directory(object):
    def __init__(self, name, chmod='0755'):
        self.__parent = None
        self.__name = name
        self.__chmod = chmod
        self.__links = []
        self.__files = {}
        self.__directories = {}
        self.__sourceDir = None
        
    def setParent(self, parent):
        self.__parent = parent
    def getParent(self):
        return self.__parent
    def getMode(self):
        return self.__chmod
    def getFiles(self):
        return self.__files.values()
    def getFile(self, fileName):
        if self.__files.has_key(fileName):
            return self.__files[fileName]
        else:
            return None
    def addLink(self, link):
        checkNotEmpty(link, 'Passed Link is None')
        if isinstance(link, Link) == False:
            raise Exception("Passed object is not a Link.")
        # TODO Check linkname already added
        self.__links.append(link)
    def addFile(self, file):
        checkNotEmpty(file, 'Passed File is None')
        if isinstance(file, File) == False:
            raise Exception("Passed object is not a File.")
        file.setParent(self)
        self.__files[file.getName()] = file
    def removeFile(self, file):
        if isinstance(file, File) == False: raise Exception("Passed object is not a File.")
        if self.__files.has_key(file.getName()):
            self.__files.pop(file.getName())
            file.setParent(None)
    def getLinks(self):
        return self.__links
    def getName(self):
        return self.__name
    def __str__(self):
        res = "Directory {name=" + str(self.__name) + ", chmod=" + str(self.__chmod) + ", content={"
        for i in self.__directories.values():
            res += str(i) + ";"
        for i in self.__links:
            res += str(i) + ";"
        for i in self.__files.values():
            res += str(i) + ";"
        return res + '}}'
    def setSourceDir(self, sourceDir):
        self.__sourceDir = sourceDir
    def getSourceDir(self):
        return self.__sourceDir
    def addDirectory(self, directory):
            checkNotEmpty(directory, 'Passed directory is None')
            if isinstance(directory, Directory) == False:
                raise Exception("Passed object is not a Directory.")
            if directory == None:
                raise Exception("passed object is null!")
    
            if self.__directories.has_key(directory.getName()):
                self.getDirectory(directory.getName()).addDirectoryContent(directory)
            else:
                self.__directories[directory.getName()] = directory
            directory.setParent(self)
    def getDirectories(self):
        return self.__directories.values()
    def getDirectory(self, name):
        if name != None and self.__directories.has_key(name):
            return self.__directories[name]
        else:
            return None
        
    def getPath(self):
        if self.getParent() != None:
            return os.path.join(self.getParent().getPath(), self.getName())
        else:
            return ""
    def getCopy(self):
        #ret = Directory(self.__parent, self.__name, self.__chmod)
        #for i in self.__links:
        #    ret.addLink(i.getCopy())
        #for i in self.__files:
        #    ret.addFile(i.getCopy())
        return copy.deepcopy(self)
    
    def printNice(self, spacer = ""):
        res = self.getName()
        for i in self.__directories.values():
            res += "\n" + spacer + " \-" + i.printNice(spacer = spacer + "  ")
             
        for f in self.__files.values():
            res += "\n" + spacer + "  |-" + str(f) 
            
        for l in self.__links:
            res += "\n" + spacer + "  |-" + str(l)
        return res
        
    def addDirectoryContent(self, directory):
        for f in directory.getFiles():
            self.addFile(f)
        for d in directory.getDirectories():
            self.addDirectory(d)
        for l in directory.getLinks():
            for i in range(0,len(self.__links)):
                if self.__links[i].getSource() == l.getSource():
                    self.__links[i] = l
            
        


class File(object):
    '''
    A Class representing a File in Unix/Linux
    '''
    def __init__(self, name, source, chmod='0644'):
        self.__source = source
        chmod = tools.setDefaultIfNull(chmod, '0644')
        self.__chmod = chmod
        self.__name = name
        self.__parent = None
        self.__isUnPackRequired = False
    def getName(self):
        return self.__name
    def getSource(self):
        return self.__source
    def setSource(self, sourceDir):
        checkNotEmpty(sourceDir, 'Passed SourceUrl is None')
        self.__source = sourceDir
    def getParent(self):
        return self.__parent
    def setParent(self, newParent):
        if newParent != None and isinstance(newParent, Directory) == False:
                raise Exception("Passed object is not a Directory but %s" %newParent)
        if self.getParent() != None : self.getParent().removeFile(self)
        self.__parent = newParent
    def getMode(self):
        return self.__chmod
    def setIsUnpackRequired(self, flag):
        self.__isUnPackRequired = flag
    def getIsUnpackRequired(self):
        return self.__isUnPackRequired
    def __str__(self):
        return "File {name=%s,source=%s,chmod=%s,unpack=%s}" %(self.__name, self.__source,self.__chmod, self.__isUnPackRequired)
    def getCopy(self):
        return copy.deepcopy(self)


class Link(object):
    '''
    A Class representing a Link in Unix/Linux
    '''
    __parentDir = None
    __target = None
    __source = None
    def __init__(self, parentDir, source, target):
            self.__target = target
            self.__parentDir = parentDir
            self.__source = source

    def getTarget(self):
            return self.__target
    def getSourceParentDir(self):
            return self.__parentDir
    def getSource(self):
            return self.__source
    def getCopy(self):
        return copy.deepcopy(self)
    def __str__(self):
        return "Link {name=%s, target=%s}" %(self.getSource(), self.getTarget())


class BaseStartScript(object):
    # the temporary file where this information had been generated
    tmpFile = None
    def __init__(self, main=None, procName=None):
        self.__main = main
        self.__procName = procName
        self.__envVariables = {}
        self.__mainArgs = []
        self.__preexecCode = None
        self.__output = None
        self.__appName = None
        self.__appVersion = None
        
        # -- begin workound-- #
        # make A.Blands vistar working : no other uses cases are known for that. Great potential to be be removed in the future.
        #
        self.__installDir = None
        # -- end workound-- #
        
    def dropOutPut(self):
            self.__output = "> /dev/null 2>&1"
    def setOutPut(self, file):
        if file != None:
            self.__output = file
        else:
            raise Exception("Invalid value :" + str(file))

    def setMain(self, mainClassName):
        self.__main = mainClassName
    def getMain(self):
        return self.__main
    def setProcessName(self, name):
        self.__procName = name
    def getProcessName(self):
        return self.__procName
    def setMainArgs(self, args):
        self.__mainArgs = args
    def getMainArgs(self):
        return self.__mainArgs
    def setEnviromentVar(self, name, value):
        self.__envVariables[name] = value
    def getEnviromentVars(self):
        return self.__envVariables
    def setPreExecutionCode(self, code):
        if code != None and len(code) > 0:
            self.__preexecCode = code
    def getPreExecutionCode(self):
        return self.__preexecCode
    def setAppName(self, appName):
        self.__appName = appName
    def getAppName(self):
        return self.__appName
    def getAppVersion(self):
        return self.__appVersion
    def setAppVersion(self, version):
        self.__appVersion = version
    def isGlobal(self):
        if self.__procName == None:
                return True
        else:
                return False
                
    # -- begin workound-- #
    # make A.Blands vistar working : no other uses cases are known for that. Great potential to be be removed in the future.
    #
    def setInstallDir(self, path):
        self.__installDir = path
    def getInstallDir(self):
        return self.__installDir
    # -- end workound-- #        

        
    #def toString(self):
    #    return "BaseStartScript : main=%s, procName=%s, mainArgs=%s, output=%s, envVariables=%s, \npreExecCode=%s" % (self.getMain(), self.getProcessName(), self.getMainArgs(), self.generateOutPut(), self.getEnviromentVars(), self.getPreExecutionCode())
    def getCopy(self):
#        ret = BaseStartScript()
#        ret.__main = copy.copy(self.__main)
#        ret.__procName = copy.copy(self.__procName)
#        ret.__preexecCode = copy.copy(self.__preexecCode)
#        ret.__tmpFile = copy.copy(self.__tmpFile)
#        ret.__mainArgs = copy.deepcopy(self.__mainArgs)
#        ret.__output = copy.copy(self.__output)
#        ret.__envVariables = copy.deepcopy(self.__envVariables)
#        ret.__installDir = copy.deepcopy(self.getInstallDir())
        return copy.deepcopy(self)


    def generateOutPut(self):
        if self.__output != None:
            return str(self.__output)
        else:
            return ''


    def generateStartScript(self):
        ''' Generates the start script and returns the name of the temporary file. '''
        output = []
        output.append('#!/bin/bash')
        output.append('')
        output.append('# Generated shell start script for ' + self.__procName)
        output.append('')
        output.append('')
        
        if self.__installDir != None:
            # -- begin workound-- #
            # make A.Blands vistar working : no other uses cases are known for that. Great potential to be be removed in the future.
            #
            output.append('export INSTALL_DIR=%s' %self.getInstallDir())
        else:
            output.append('INSTALL_DIR=`dirname $0` ; [[ $INSTALL_DIR == "." ]] && INSTALL_DIR=$PWD')
            output.append('export INSTALL_DIR=$INSTALL_DIR/../')
            # end workound #
        output.append("cd $INSTALL_DIR")
        output.append('')
        
        #the user enviroment variables
        for env in self.__envVariables.keys():
            output.append('''export %s="%s" ''' % (env, self.__envVariables[env]))
        output.append('')
        if self.__preexecCode != None:
            output.append('# --- USER PRE-EXECUTION CODE ---')
            output.append(self.__preexecCode)
            output.append('# --- ---')
            output.append('')
        cmd = []
        cmd.append('exec -a "$(basename $0)"')
        cmd.append(self.__main)

        for arg in self.__mainArgs:
            cmd.append(arg)

        cmd.append(self.__generateOutPut__())
        output.append(" ".join(cmd))

        output.append('')
        # create the tmporary start script
        tmp = tempfile.mkstemp()
        os.write(tmp[0], "\n".join(output))
        os.close(tmp[0])
        self.tmpFile = tmp[1]
        return tmp[1]

    def getTmpFile(self):
        return self.__tmpFile

    def __str__(self):
        return "BaseStartScript{main=%s, procName=%s, mainArgs=%s, output=%s, envVariables={%s}, \npreExecCode=%s" % (self.getMain(), self.getProcessName(), self.getMainArgs(), self.generateOutPut(), self.getEnviromentVars(), self.getPreExecutionCode())
 
        


class JavaStartScript(BaseStartScript):
    ''' Class which represents internally all information about a java startscript.'''

    def __init__(self, main=None, procName=None):
        super(JavaStartScript, self).__init__(main, procName)
        self.__userProps = {}
        self.__jvmMem = ''
        self.__jvmProps = ''

    def getCopy(self):
        #ret = JavaStartScript(copy.copy(self.getMain()), copy.copy(self.getProcessName()))
#        ret.setJvmMemArgs(copy.copy(self.getJvmMemArgs()))
#        ret.setJvmProps(copy.copy(self.getJvmProps()))
#        ret.__tmpFile = None
#        ret.__userProps = copy.deepcopy(self.getUserProps())
#        ret.setMainArgs(copy.deepcopy(self.getMainArgs()))
#        ret.__envVariables = copy.deepcopy(self.getEnviromentVars())
#        ret.setPreExecutionCode(copy.copy(self.getPreExecutionCode()))
#        ret.setAppName(self.getAppName())
#        ret.setAppVersion(self.getAppVersion())
        return copy.deepcopy(self)


    def setUserProp(self, prop, value):
        self.__userProps[prop] = value
    def getUserProps(self):
        return self.__userProps
    def setJvmMemArgs(self, jvmMem):
        self.__jvmMem = jvmMem
    def getJvmMemArgs(self):
        return self.__jvmMem
    def setJvmProps(self, jvmArgs):
        self.__jvmProps = jvmArgs
    def getJvmProps(self):
        return self.__jvmProps
    def checkMySelf(self):
        tools.checkNotEmpty(self.getMain(), "main class is not set: \n" + str(self))
        self.setProcessName(self.getProcessName().upper())
        if not self.getProcessName().endswith(".jvm"):self.setProcessName(self.getProcessName() +".jvm")

    def generateStartScript(self):
        self.checkMySelf()
        ''' Generates the start script and returns the name of the temporary file. '''
        output = []
        output.append('#!/bin/bash')
        output.append('')
        output.append('# Generated shell start script for ' + self.getProcessName())
        output.append('')
        output.append('')
        output.append('[[ -z "$JAVA_HOME" ]] && export JAVA_HOME=/usr/java/jdk')
        if self.getInstallDir() != None:
            #
            # workaround to make A.Blands vistar working : no other uses cases are known for that. Potential to be be removed in the future.
            #
            output.append('export INSTALL_DIR=%s' %self.getInstallDir())
        else:
            output.append('INSTALL_DIR=`dirname $0` ; [[ $INSTALL_DIR == "." ]] && INSTALL_DIR=$PWD')
            output.append('export INSTALL_DIR=$INSTALL_DIR/../')
            # end workound #
        output.append("CLASSPATH=`ls $INSTALL_DIR/lib/*.jar | tr -s '\\n' ':'`")
        output.append("cd $INSTALL_DIR")
        output.append('')

        #the user enviroment variables
        envVars = self.getEnviromentVars()
        for env in envVars.keys():
                output.append('''export %s="%s" ''' % (env, envVars[env]))

        # here the JVM user properties
        output.append('JVM_MEM="%s"' % self.__jvmMem)
        output.append('JVM_OTHER_OPTS="%s"' % self.getJvmProps())
        output.append('')
        if self.getPreExecutionCode()!= None:
            output.append('# --- USER PRE-EXECUTION CODE ---')
            output.append(self.getPreExecutionCode())
            output.append('# --- ---')
            output.append('')

        cmd = []
        cmd.append('exec -a `basename $0`')
        cmd.append('$JAVA_HOME/bin/java')
        cmd.append('-cp "$CLASSPATH"')
        props = self.getUserProps()
        for property in props.keys():
            if props[property] != None:
                cmd.append('-D' + property + '="' + props[property] + '"')
            else:
                cmd.append('-D' + property)
        cmd.append('-Dapp.name="%s" ' % self.getAppName())
        cmd.append('-Dapp.version="%s" ' % self.getAppVersion())
        cmd.append('$JVM_MEM')
        cmd.append('$JVM_OTHER_OPTS')
        cmd.append(self.getMain())

        for arg in self.getMainArgs():
                cmd.append(arg.replace('\n',''))
        cmd.append(self.generateOutPut())
        output.append(" ".join(cmd))

        output.append('')

        # create the tmporary start script
        tmp = tempfile.mkstemp()
        os.write(tmp[0], "\n".join(output))
        os.close(tmp[0])

        self.tmpFile = tmp[1]
        return tmp[1]
    
    def __str__(self):
        return "JavaStartScript {main=%s, procName=%s, mainArgs=%s, output=%s, envVariables={%s}, appName=%s, JVM_ARGS={%s}, JVM_MEM={%s}, USERJVMOPTS={%s}}" %(self.getMain(), self.getProcessName(), self.getMainArgs(), self.generateOutPut(), self.getEnviromentVars(), self.getAppName(),self.getJvmProps(),self.getJvmMemArgs(),self.getUserProps())
        