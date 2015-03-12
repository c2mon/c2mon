'''
    Library for the deploy tool.
    
    Contains functions for reading information from the deployment configuration file.
    
    F.Ehm, CERN 2011  
'''

import logging
import os
from copy import copy

from common import Product
from common import Application
from common import LogRotateFile
from common import SimpleFile
from common import BackupPolicy
from common import DeploymentInfo
from common import Link
from common import Directory
from common import File
from common import BaseStartScript
from common import JavaStartScript
from common import Notification

import re
from tools import setDefaultIfNull
from tools import readXmlFromFile
from tools import checkNotEmpty

import libxml2
from libxml2 import xmlNode

log = logging.getLogger('config')

#
#def getProductsFromConfig(file):
#
#    doc = libxml2.parseFile(file)
#    
#    ret = {}
#    for p in doc.xpathEval('//product'):
#       
#        version = None
#        alias = None
#        sourceDir = None
#        name = None
#
#        if p.hasProp('name') == None:
#          raise Exception("You need to have a name attribute in <product> !")
#        name = p.hasProp('name').getContent()
#        if p.hasProp('version') != None:
#            version = p.hasProp('version').getContent()
#        if p.hasProp('directory') != None:
#            directory = p.hasProp('directory').getContent()
#        if p.hasProp('alias') != None:
#            alias = p.hasProp('alias').getContent()
#   if p.hasProp('type') != None:
#            pType = p.hasProp('type').getContent()
#            
#        log.debug("Attempt to read <product> with name='%s'." %name)
#
#        result = Product(name)    
#        result.setSourceUrl(directory)
#        result.setVersion(alias, version)
#        result.setDeployConfig(file)
#        
#        oldStyleDeploymentInfo = getChildNodes(p, 'deployment')
#        defaultApp = Application(result, "default")
#        
#        if len(oldStyleDeploymentInfo) == 1 :
#            log.debug("Found %d old-style deployment tag." %len(oldStyleDeploymentInfo))
#            depInfo = readDeploymentTag(defaultApp, oldStyleDeploymentInfo[0])
#            defaultApp.setDeploymentInfo(depInfo)
#        
#        for app in getChildNodes(p, 'application'):
#            app = readNormalApplicationTag(result, app)
#            if app.getDeploymentInfo().getInstallLocation() != None:
#               app.setInstallDir(app.getDeploymentInfo().getInstallLocation())
#            else:
#               app.setInstallDir('/opt/%s/%s' %(result.getName(),app.getName()))
#            result.addApplication(app)
#        
#        if result.hasApplication('default'):
#            if len(result.getApplications()) != 1:
#                result.removeApplication('default')
#            else:
#                raise Exception("You have given the application name 'default'. This is not allowed. please give it nice descriptive name!")
#        for app in result.getApplications():
#            if len(app.getStartScripts()) != 1:
#                app.removeStartScript('default')
#        
#
#        ret[result.getName()] = result
#    return ret




def getProductsFromConfig(confFile):
    '''
        Gets all available products from the file. 
        @return a DICT containing the found Products. No complete product object with applications is returned, only simple information.
        @param the deployment.xml (product.xml)
    '''
    
    log.debug("Reading available products from file %s" %confFile)

    doc = libxml2.parseFile(confFile)
    products = doc.xpathEval('//product')
    if len(products) == 0:
         raise Exception("No products have been found in %s" %confFile)
     
    result = {}
    for tag in products:
        name = tag.hasProp('name').getContent()
        version = tag.hasProp('version').getContent()
        sourceUrl = None

        if tag.hasProp('directory'):
            sourceUrl = tag.hasProp('directory').getContent()
        elif tag.hasProp('sourceUrl'):
            sourceUrl = tag.hasProp('sourceUrl').getContent()
        p = Product(name)
        p.setSourceUrl(sourceUrl)
        p.setDeployConfig(confFile)
        p.setVersion(None,version)
        result[name] = p
    log.debug("Returning %s" %result)
    return result


def getApplicationsFromConfig(file, product, appName = None):
    app = None

    if isinstance(product, Product) == False:
        raise Exception("passed parameter is not of Product class : %s" %product)

    doc = libxml2.parseFile(file)
    log.debug("Deployment configuration:\n" + str(doc))
    
    if len(doc.xpathEval('//product')) == 1:
        doc = doc.xpathEval('//product')[0]
    else:
        doc = doc.xpathEval('//product[@name="' + product.getName() + '"]')

    globalApp = doc.xpathEval('application[not(@name)]')
    if len(globalApp) == 0:
        globalApp = doc.xpathEval('application[@name="default"]')
    elif len(globalApp) > 1:
        raise Exception("You can only have 1 default OR 1 unamed application tag")
    
    # should only be 1 !
    log.debug("Reading the 'default' tag")
    defaultApp = None
    for appNode in globalApp:
        defaultApp = readNormalApplicationTag(product, appNode)
        product.addApplication(defaultApp)
    
    apps = None
    if appName != None:
        apps = doc.xpathEval('application[@name]')
    else:
        # we only consider <application> with a name attribute
        apps = doc.xpathEval('application[@name and @name!="default"]')

    for appNode in apps:
        log.debug("Reading the requested application '%s'" %appName)
        app = readNormalApplicationTag(product, appNode, defaultApp)
        if app.getDeploymentInfo().getInstallLocation() != None:
           app.setInstallDir(app.getDeploymentInfo().getInstallLocation())
        product.addApplication(app)

    # remove 'default' app, so it is not generated
    if product.hasApplication('default'):
        if len(product.getApplications()) != 1:
            product.removeApplication('default')
        else:
            # we have one 'default' application. 
            # For CMB this is not allowed but made sure before by the release system
            # For Maven this may be allowed but doesn't make sence.
            raise Exception("You have given the application name 'default'. This is not allowed. please give it nice descriptive name!")

    # remove the 'default' startscript (might have been inherited before)
    for app in product.getApplications():
        if len(app.getStartScripts()) != 1:
            app.removeStartScript('default')
            
    return product




def getAttribute(tag, name):
    val = tag.xpathEval('@' + name)
    if val != None and len(val) > 0:
         return val[0].content
    else:
         return None
def getChildNodes(tag, nodeName):
    val = tag.xpathEval(nodeName)
    if val != None and len(val) > 0:
         return val
    else:
         return []

def checkValid(tag, expect):
    if tag == None:
        return None
    if not isinstance(tag, xmlNode):
        raise Exception("passed object is not an xmlNode : %s" %tag)
    if tag.get_name() != expect:
        raise Exception("Passed argument is not an <%s> but of '%s'" %(expect, tag.get_name()))
    return True
    
    
    
def readNormalApplicationTag(product, appNode, toInheritFrom = None):
    name = setDefaultIfNull(getAttribute(appNode, "name"), 'default')
    extends = getAttribute(appNode, "extends")
    newApp = None
    toInheritStartscript = None
    
    if toInheritFrom != None:
        log.debug("Inheriting from application '%s' for '%s'" %(toInheritFrom.getName(),name))
        newApp = toInheritFrom.getCopy()
    else:
        newApp = Application(product, name)
    newApp.setName(name)
   
    for s in getChildNodes(appNode, 'startscript'):
        toInheritStartscript = None
        sname = getAttribute(s, 'name')

        if name != None:
            if newApp.getStartScript(sname) == None: sname = None
            toInheritStartscript = newApp.getStartScript(sname)
            log.debug("Setting parent script for inheritance to %s" %str(toInheritStartscript))
        else:    
            # if we are just about to read a default startscript, we must not inherit 
            toInheritStartscript = None
            
        
        # ok, got parent. Lets read the info for a startscript 
        script = readJavaStartScript(s, newApp.getName(), toInheritStartscript)
        
        script.setAppVersion(product.getRealVersion())
        log.debug("Setting AppName = '%s'" %name)
        script.setAppName(name)
        log.debug("Startscript for app %s with name %s" %(name, script.getProcessName()))
        if name != 'default' and (script.getProcessName() == None or script.getProcessName() == 'default') :
            log.debug("Setting ProcName = '%s'" %name)
            script.setProcessName(name)
        newApp.addStartScript(script)
    
    
    deploymentInfo = getChildNodes(appNode, 'deployment')
    if len(deploymentInfo) > 0:
        deploymentInfo = deploymentInfo[0]
        depInfo = readDeploymentTag(newApp, deploymentInfo)
        newApp.setDeploymentInfo(depInfo)
    
    for s in newApp.getStartScripts():
        s.setAppName(name)
        if name != 'default' and s.getProcessName() == None:
            newApp.removeStartScript(None)
    return newApp
    

def readLogRotateFromTag(logrotateTag):
    if checkValid(logrotateTag, 'logrotate') == None:
        return None
    file = LogRotateFile(logrotateTag.hasProp('name').getContent())
    file.setMaxFileSize(logrotateTag.hasProp('fileSize').getContent())
    return file

def readSimpleFileFromTag(fileTag):
    checkValid(fileTag, 'file')
    file = SimpleFile(fileTag.hasProp('name').getContent())
    return file



def readOutPutTag(outputNode):
    ''' Reads the <output> tag and return either None, 'NULL', or a valid LogRotateFile obecjt
    '''
    checkValid(outputNode, 'output')        
    
    if len(getChildNodes(outputNode, 'devNull')) > 0:
        return 'NULL'
    elif len(getChildNodes(outputNode, 'logrotate')) > 0:
        return readLogRotateFromTag(getChildNodes(outputNode, 'logrotate')[0])
    elif len(getChildNodes(outputNode, 'file')) > 0:
        return readSimpleFileFromTag(getChildNodes(outputNode, 'file')[0])
    else:
        raise Exception('<output> needs one of children : <devNull/>, <logrotate/>, <file/>')

def readPreExecCode(preExecNode):
    ''' 
        Reads CDATA from the <preExec> tag
    '''
    checkValid(preExecNode, 'preExec')
    return preExecNode.getContent()


def readJvmMemorySettings(applicationNode):
    '''     Reads and validates
            <application ..>
              <initial-heap-size> </initial-heap-size>
              <max-heap-size> </max-heap-size>
            </application ..>
            It returns a valid string or None in case nothing has been found.
    '''
    resultInitHeap = ""
    resultMaxHeap = ""
    
    initHeap = getChildNodes( applicationNode, "initial-heap-size")
    if len(initHeap) > 0:
        initHeap = initHeap[0].getContent()
        if not re.match("\d{1,4}[M|m]", initHeap) and not re.match("\d{1,2}[G|g]", initHeap):
            raise Exception("The initial-heap-size value is not valid : " + initHeap)
        resultInitHeap = "-Xms" + initHeap
    else:
        initHeap = ""
    
    maxHeap = getChildNodes( applicationNode, "max-heap-size")    
    if len(maxHeap) > 0:
        maxHeap = maxHeap[0].getContent()    
        if not re.match("\d{1,4}[M|m]", maxHeap) and not re.match("\d{1,2}[G|g]", maxHeap):
            raise Exception("The max-heap-size value is not valid : " + maxHeap)
        resultMaxHeap = "-Xmx" + maxHeap
    else:
        maxHeap = ""


    if initHeap.lower() != maxHeap.lower():
        log.warning("Your init-heap is not the same as the max-heap. However, for server processes it is advised to set them equal!")
    if resultMaxHeap != "":
        resultInitHeap = resultInitHeap + " " + resultMaxHeap
        
    return resultInitHeap


def readJavaVmArgs (applicationNode):
    ''' Reads <java-vm-args>
    '''
    # optional, no check required
    # checkValid(applicationNode, 'java-vm-args')
    
    result = getChildNodes( applicationNode, "java-vm-args")
    if len(result) > 0:
        return result[0].getContent()
    return ""


#
# read the <backup> tag
#
def readBackupInformation(backupNode, inherit=None):
    if checkValid(backupNode, 'backupPolicy') == None:
        return None

    if inherit != None:
        result = inherit.getCopy()
    else:
        result = BackupPolicy()

    nbBackups = setDefaultIfNull(getAttribute(backupNode, 'keepBackups'), result.getKeepBackups())
    if nbBackups == None:
        log.warning("No 'keepBackups' attribute in <backupPolicy> found. Taking current default=%s" %(nbBackups))
        
    else:
        val = nbBackups
        try:
            nbBackups = int(val)
        except:
            raise Exception("Couldn't evaluate 'keepBackups' value : " + val)

    # ruls to ensure
    if nbBackups > 5 or nbBackups < 0:
        raise Exception('Number of backups must be 0 <= x <= 5. Given : ' + str(nbBackups))
    else:
        # ok with backup
        result.setKeepBackups(nbBackups)

    # to check if we have to preserve files
    for tag in getChildNodes(backupNode, 'preserve'):
        if nbBackups == 0:
            log.warning("You want to preserve files, but haven't configured to keep backups.")
            nbBackups = 1
        type = tag.hasProp('type').getContent()
        name = tag.hasProp('name').getContent()
        if type == 'dir':
            log.debug ("Found directory to preserve : %s" % name)
            result.addPreserveDir(name)
        elif type == 'file':
            log.debug ("Found file to preserve : %s" % name)
            result.addPreserveFile(name)

    log.debug('Found : ' + str(result))
    return result


def readNotification(notificationNode, inherit=None):
    ''' reads the <notification> configuration '''

    if checkValid(notificationNode, 'notification') == None:
        return None

    result = []

    log.debug('Reading notification information')
    for item in getChildNodes(notificationNode, 'mail'):
        notification = Notification(item.hasProp('address').getContent())
        if item.hasProp('sendAtRestart') != None:
            notifcation.setSendAtRestart(item.hasProp('sendAtRestart').getValue())
        result.append(notification)
        log.debug("Found notification : " + str(notification))
    return result


def readStringReplacement(replacementNode, inherit=None):
    ''' 
        Reads <replaceString>.
        @param replacementNode The xmlNode containing <replaceString><file name="" var="" value="" ></replaceString>
        @param inherit a hashmap to copy over. May be None
        @return a hashmap with the filename as the key and as value an array of (searchString, replaceString)
    '''
    
    result = {}
    if inherit != None:
        result = copy(inherit)
    
    if checkValid(replacementNode, 'replaceString') == None:
        return None
    
    log.debug('Reading string replacement information')
    result = {}
    for file in getChildNodes(replacementNode,'file'):
        fileName = file.hasProp('name').getContent()
        var = file.hasProp('var').getContent()
        value = file.hasProp('value').getContent()
    if not result.has_key(fileName):
        result[fileName] = []
        my = (var, value)
        result[fileName].append(my)
        log.debug('Found replacement in %s : VAR=%s, NEW VALUE=%s' % (fileName, var, value))
    return result



def readSymbolicLinkInformation(parent, linkNode):
    '''
        reads <link target="" source="" />
    '''
    if checkValid(linkNode, 'link') == None:
        return None
        
    target = linkNode.hasProp('target').getContent()
    source = linkNode.hasProp('source').getContent()
    lnk = Link(parent, source , target)
    log.debug('Found symbolic link : source = %s , target = %s' % (source, target))
    return lnk



def readDeploymentTag(application, deploymentTag):
    '''
        reads <deployment> info.
        It re-uses the Deployment from the passed application object. 
    '''
    depInfo = application.getDeploymentInfo()
    
    if checkValid(deploymentTag, 'deployment') == None:
        return None
    if isinstance(application, Application) == False:
        raise Exception("first argument must be Application class")

    log.debug('Found deployment information. Reading it...')
    installLoc = None
    # set the installation path only if nothing has been given to us
    # as a cmd line  parameter to overrride
    tmp = getAttribute(deploymentTag, 'installLocation')
    if tmp != None:
        installLoc = os.path.expanduser(tmp)
        # protection that we do not have '/' at the end of a path. this causes propblems for tools.makeBackup()
        if installLoc[len(installLoc)-1] == '/':
             installLoc = installLoc[:len(installLoc)-1]

        log.debug("Found installationLocation=%s" % installLoc)
        depInfo.setInstallLocation(installLoc)

    rootInstallDir = depInfo.getDirectoryStructure()

    for d in getChildNodes(deploymentTag, 'directory'):
        childDir = readDirectory(d)
        rootInstallDir.addDirectory(childDir)
    for f in getChildNodes(deploymentTag, 'file'):
        childFile = readFileTag(f)
        rootInstallDir.addFile(childFile)
    for l in getChildNodes(deploymentTag, 'link'):
        childLink = readSymbolicLinkInformation(rootInstallDir, l)
        rootInstallDir.addLink(childLink)
    
    log.debug("Found rootStructure : \n" + rootInstallDir.printNice())
    
    depInfo.setDirectoryStructure(rootInstallDir)
        

    # now the backup policy
    if len(getChildNodes(deploymentTag, 'backupPolicy')) > 0:
        backupPolicy = readBackupInformation (getChildNodes(deploymentTag, 'backupPolicy')[0], depInfo.getBackupPolicy())
        depInfo.setBackupPolicy(backupPolicy)

    # don't forget to read the notification, in case sombody is interested whenever the product is installed
    if len(getChildNodes(deploymentTag, 'notification')) > 0:
        depInfo.addNotificationList(readNotification(getChildNodes(deploymentTag, 'notification')[0], depInfo.getNotificationList()))
    
    if len(getChildNodes(deploymentTag, 'replaceString')) > 0:
        depInfo.setStringReplacements(readStringReplacement(getChildNodes(deploymentTag, 'replaceString')[0], depInfo.getStringReplacements()))

    if len(getChildNodes(deploymentTag, 'signJars')) > 0:
        if depInfo.getJarSignerConfig() != None:
            raise Exception("It is not allowed to have more than one signjars tag. Apparently you have one already specified (inherited?)")
        depInfo.setJarSignerConfig(readJarSignerTag(getChildNodes(deploymentTag, 'signJars')[0]))

    #
    #if len(deploymentTag.getElementsByTagName('replaceString')) > 0:
    #        readStringReplacement( deploymentTag.getElementsByTagName('replaceString')[0] )
    return depInfo

def readDirectory(directoryTag, inherit=None ):
    '''
    Reads all <directory> and <file> items from the passed tag
    
    @return a valid Directory with its children
    '''
    
    # the directory which we are investigating
    dir = None
    
    if checkValid(directoryTag, 'directory') == None:
        return None
    
    # read the name first
    name = directoryTag.hasProp('name').getContent()
    log.debug('Found directory : ' + name)
    
    if inherit != None:
        dir = inherit.getCopy()
    else:
        dir = Directory(name)
    
    # specialised mask ?
    mask = setDefaultIfNull(getAttribute(directoryTag, "mask"), dir.getMode())
    if len(mask) == 3:
        mask = '0' + mask    
        
    # do we need to fill it with content from other directory ?
    dir.setSourceDir(setDefaultIfNull(getAttribute(directoryTag, "sourceDir"), dir.getSourceDir()))
    
    
    # add other (sub)directories
    for d in getChildNodes(directoryTag, 'directory'):
        child = readDirectory(d)
        dir.addDirectory(child)
    for f in getChildNodes(directoryTag, 'file'):
        child = readFileTag(f)
        dir.addFile(child)
    for l in getChildNodes(directoryTag, 'link'):
        child = readSymbolicLinkInformation(dir, l)
        dir.addLink(child)
    # Directory
    return dir


def readFileTag(fileTag):
    if checkValid(fileTag, 'file') == None:
        return None

    fileName = fileTag.hasProp('name').getContent()
    
    # optional
    sourceDir = getAttribute(fileTag, 'sourceDir')
    source = getAttribute(fileTag, 'source')
    mask = getAttribute(fileTag, 'mask')
    unpack = getAttribute(fileTag, 'unpack')

    if fileName.rfind(os.path.sep) > 0:
        log.error('You are using a deprecated way of creating directories. Please move "%s" this to use \n<directory name="..">\n  <directory name="..>\n    <file ..>' %fileName)
        sourceDir = os.path.dirname(fileName)
        fileName = os.path.basename(fileName)

    if sourceDir != None:
        source = os.path.join(sourceDir, fileName)
    
    if mask != None and len(mask) == 3:
        mask = '0' + mask
    if source != None:
        f = File(fileName, os.path.expanduser(source), mask)
    else:
        f = File(fileName, fileName, mask)
        
    if unpack != None and unpack.lower() == 'true':
        f.setIsUnpackRequired(True)
        
    log.debug("Found %s" %f)
    return f



def readJarSignerTag(tag, inherit=None):
    if checkValid(tag, 'signJars') == None:
        return None
    if tag.hasProp('keyStore') == None:
        raise Exception("<signJars> requires 'keyStore' attribute")
    keyStore = tag.hasProp('keyStore').getContent()
    return keyStore
    
    


def readUnpackTag(unpackTag):
    if checkValid(unpackTag, 'unpack') == None:
        return None
    sourceUrl = unpackTag.hasProp('source').getContent()
    return UnPack(sourceUrl)


def readStartscriptTag(stag, appName, inherit=None, type="JAVA"):
    if type == "JAVA":
        script = readJavaStartScript(stag, appName, inherit)
    else:
        script = readBaseStartScript(stag, appName, inherit)
    return script



def readJettyStartscript(stag, defaultName, inherit=None):
    if inherit != None:
        result = inherit.getCopy()
    else:
        result = BaseStartScript()
    name = setDefaultIfNull(getAttribute(stag, 'name'), defaultName)
    # read the <env name="" value="" />
    for env in readEnvironmentVars(getChildNodes(stag, 'env')).items():
        key = env[0]
        value = env[1]
        log.debug("Setting env var %s=%s" %(key, value))
        myScript.setEnviromentVar(key, value)



def readEnvironmentVars(tag):
    '''
        Reads <env> tag and returns a hashmap with key-value pairs
        @param tag the <env> xmwNode 
        @return a map (not None)
    '''
    result = {}
    for env in getChildNodes(tag, 'env'):
        checkNotEmpty(env.hasProp('name'), "No key in <env> found.")
        checkNotEmpty(env.hasProp('value'), "No value in <env> found.")
        key = env.hasProp('name').getContent()
        value = env.hasProp('value').getContent()
        result[key] = value
    return result
        
    


def readJavaStartScript(stag, appName, inherit=None):
    '''
       Reads startscript information. 
       @param stag the tag containing startscript information
       @param appName the name of the application the potential startscript belongs to. This is also the default name of the returned startscript
       @param inherit the startscript we should inherit information from. May be None.
    '''
    myScript = None

    # we do not check if this is a <startscript> tag because commonbuild 
    # does not support these tags and the info is stated in the normal <application>
    # ... Rubbish!
    
    if inherit != None:
        myScript = inherit.getCopy()
        log.debug("Copying global startscript configuration for " + appName + " : %s" %(myScript))
    else:
        myScript = JavaStartScript()


    log.debug("Reading startscript tag data \n" + str(stag))

    # name is optional - if None, we take the name of the application
    name = setDefaultIfNull(getAttribute(stag, 'name'), appName)
    procName = getAttribute(stag, 'name')

    log.debug("Reading Startscript %s for App %s" %(name, appName))
    if len(getChildNodes(stag, 'main-class')) > 0:
        main = getChildNodes(stag, 'main-class')[0]
        mainClass = main.hasProp('name').getContent()
        log.debug("Setting main-class %s" %(mainClass))
        myScript.setMain(mainClass)
        args = []
        for arg in getChildNodes(main, 'arg'):
            content = arg.getContent().replace("\n","").replace("\t","")
            if len(content) > 0:
                args.append(content)
        if len(args) > 0:
            log.debug("Setting main args : %s" %(str(args)))
            myScript.setMainArgs(args)

    else:
        log.debug("No main-class found")


    # read the <env name="" value="" />
    envs = readEnvironmentVars(stag)
    for e in envs.keys():
        log.debug("Setting env var %s=%s" %(e, envs[e]))
        myScript.setEnviromentVar(e, envs[e])

    if myScript.__class__ == JavaStartScript:
        # read JVM args, if given
        javaVmArg = readJavaVmArgs(stag)
        if javaVmArg != "":
            javaVmArg = '%s' % javaVmArg
            log.debug("Setting JVM args = '%s'" %javaVmArg)
            myScript.setJvmProps(javaVmArg)
        # read JVM Memory settings, if given
        javaVmMemorySettings = readJvmMemorySettings(stag)
        if javaVmMemorySettings != "":
            log.debug("Setting JVM MEM args = '%s'" %javaVmMemorySettings)
            myScript.setJvmMemArgs(javaVmMemorySettings)

        # read the User properties <property name="" value=""/>
        for property in getChildNodes(stag, 'property'):
            checkNotEmpty(property.hasProp('name'), "<property> has no 'name' attribute")
            pkey = property.hasProp('name').getContent()
            pvalue = None
            if property.hasProp('value'):
                pvalue = property.hasProp('value').getContent()
            log.debug("Setting JVM option -D%s=%s" %(pkey, pvalue))
            myScript.setUserProp(pkey, pvalue)

    output = None # 'NULL'
    # where should STDOUT and STDERR go?
    if len(getChildNodes(stag, 'output')) > 0:
        output = readOutPutTag(getChildNodes(stag, 'output')[0])
        log.debug("Setting output to %s" % (str(output)))
    if output != None:
        if output == 'NULL':
            myScript.dropOutPut()
        elif output.__class__ == LogRotateFile or output.__class__ == SimpleFile:
            myScript.setOutPut(output)
        else:
            raise Exception("Code error when reading <output> tag! ")

    if len(getChildNodes(stag, 'preExec')) > 0:
        myScript.setPreExecutionCode(readPreExecCode(getChildNodes(stag, 'preExec')[0]))

    if procName != None:
        log.debug("Setting Process Name = %s" %procName)
        myScript.setProcessName(procName)

    return myScript
