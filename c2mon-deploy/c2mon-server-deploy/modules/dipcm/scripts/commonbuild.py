'''
    Library for the deploy tool.
    
    Contains functions to handle CommonBuild projects to be installed. 
    
    F.Ehm, CERN 2011  
'''
import os
import tempfile
import logging
import config
import tools
import re

import libxml2

from common import Product
from common import Directory
from common import File
from common import JavaStartScript
from common import Application
from common import InstallConfig



log = logging.getLogger('cmnbuild')


class Commonbuild(object):
    '''
    classdocs
    '''
    product = None
    installConfig = None
    myRepo = None



    def __init__(self, installConf):
        '''
        Constructor
        '''
        if isinstance(installConf, InstallConfig) == False:
            raise Exception("Passed parameter is not a InstallConfig object.")
        
        
        if installConf.getVersion() == None: installConf.setVersion('PRO')
        self.installConfig = installConf
        
        # A config file is given
        #
        if installConf.getDeployConfig() != None:
            products = config.getProductsFromConfig(installConf.getDeployConfig())

            if len(products) == 0:
               raise Exception("No products found in '%s' " %installConf.getDeployConfig())
            elif len(products) == 1:
               installConf.setProductName(products.values()[0].getName())
            elif not products.has_key(installConf.getProductName()):
               raise Exception("cannot find product '%s' in '%s' " %(installConf.getProductName(), installConf.getDeployConfig()))


            product = getProductFromConfig(installConf.getDeployConfig(), installConf.getProductName())
            p = getApplicationsFromConfigOldStyle(product)

            # set the source url to default (where the config file is)
            if p.getSourceUrl() == None:
               log.debug("Setting config location as source url because none is defined in the file")
               p.setSourceUrl(os.path.abspath(os.path.dirname(installConf.getDeployConfig())))
        
            # override the default sourceUrl of the product
            if installConf.getSourceUrl() != None:
                p.setSourceUrl(installConf.getSourceUrl())
        
            p.setVersion(p.getRealVersion(), 'LOCAL_BUILD')
            installConf.setVersion("LOCAL_BUILD")
            
            p.setReleaseDate("UNKNOWN")
            self.product = p
        

        #
        # No source url, no config : get everything from the repository
        #
        else:
            myRepo = RepoInfo(self.installConfig.getProductName())
            self.product = myRepo.getProduct(self.installConfig.getVersion())
            self.product = getApplicationsFromConfigOldStyle(self.product)
        

     
    def install(self, appName=None):

        # TODO move reading of the deployment config here.

        apps = self.product.getApplications()
        
        if len(apps) == 1:
            log.info("Found only one Application : %s" %apps[0].getName())
            self.installConfig.setApplicationName(apps[0].getName())
        elif len(apps) == 0:
            raise Exception("I haven't found any application in the product.xml.")
 
        if appName == None:
            appName = self.installConfig.getApplicationName()

        if not self.product.hasApplication(appName):
            raise Exception("Can't find Application '%s' for product %s and version %s " %(appName, self.product.getName(), self.product.getRealVersion()))
        
        application = self.product.getApplication(appName)
        
        rootDir = application.getDeploymentInfo().getDirectoryStructure()
     
        if os.path.isdir(self.product.getSourceUrl()):
            prefixFilesWithSourceDir(rootDir,self.product.getSourceUrl())
            
        # Add default directories
        #
        for d in ['bin','log']:
            if rootDir.getDirectory(d) == None:
                log.debug("Adding empty Directory '%s' " %d)
                rootDir.addDirectory(Directory(d))
        
        # Add deployment config
        #
        rootDir.addFile(File('product.xml', self.product.getDeployConfig()))
        
        # Add old-style commonbuild subdirectory copy mechanism
        #
        if os.path.exists(os.path.join(self.product.getSourceUrl(), 'src', self.product.getName())):
            path = os.path.join(self.product.getSourceUrl(), 'src', self.product.getName())
            log.warn("Found old-style commonbuild way of copying folder (subfolder with projectname as name. Please move to new way using <directory name=\"..\" sourceDir=\"..\">")
            for i in os.listdir(path):
                if os.path.isfile(os.path.join(path,i)):
                    f = File(i, os.path.join(path,i))
                    rootDir.addFile(f)
        else:
                    d = Directory(i)
                    d.setSourceDir(os.path.join(path,i))
                    rootDir.addDirectory(d)
        
        libs = Directory('lib', chmod='0744')
        webapp = Directory('webapp', chmod='0744')
        log.info("Creating dependency file list..")
        for dir in ('build/dist/', 'lib'):
            tmp = os.path.join(self.product.getSourceUrl(), dir)
            if not os.path.exists(tmp): continue
            for file in os.listdir(tmp):
                target = os.path.join(tmp, file)
                if re.match(".*\.jar", file):
                    libs.addFile(File(file, target))
                if re.match(".*\.war", file):
                    webapp.addFile(File(file, target))
                    
        # remove all jars which were marked as exluded 
        for i in self.getExludedJars():
            libs.removeFile(File(i,None))
        
        rootDir.addDirectory(libs)
        if len(webapp.getFiles()) > 0:
            rootDir.addDirectory(webapp)
        
        
        log.debug("Final Layout : root=%s \n%s" %(application.getInstallDir(), rootDir.printNice()))
        
        # read the changelog (if given)
        try:
            self.product.setChangeLog(tools.readChangelog(os.path.join(self.product.getSourceUrl(), "changelog.txt")))
        except KeyError, ex:
            # IGNORE : means there is not changelog in the tar.gz
            pass

        tools.doPreInstall(application, self.installConfig)

        if not os.path.isdir(self.product.getSourceUrl()):
            log.info("Extracting code base from %s in %s" %(self.product.getSourceUrl(), self.installConfig.getInstallDir()))
            tools.extractTar(self.product.getSourceUrl(), self.installConfig.getInstallDir())
       
        #tools.extractTar(fileName, application.getInstallDir())
 
        # copy the libraries to the lib folder
        
        tools.doPostInstall(application, self.installConfig)
        
    def getProduct(self):
        return self.product
        
        
        
    def getExludedJars(self):
        result = []
        excludedPath = os.path.join(self.product.getSourceUrl(), 'lib', 'excluded')
        if not os.path.exists(excludedPath) : return result
        for f in os.listdir(excludedPath):
            result.append(f.replace(".excluded",""))
        log.debug("Found %d files to be excluded : %s" %(len(result), result))
        return result
       
       
            
def addFilesByRegExp(dir, regexp, sourceDir=None):
        import glob
        result = []
        if sourceDir != None: regexp = os.path.join(dir, regexp)
        log.debug("REGEXP: Adding files in '%s' to '%s' "%(regexp, dir.getName()))
        for name in glob.glob(regexp):
            f = File(os.path.basename(name), name)
            f.setSource(name)
            log.debug("adding file '%s' from regexp " %(f))
            dir.addFile(f)
        return result

def prefixFilesWithSourceDir(rootDir, productSourceUrl):
    '''
      Prefix all files with the source directory where they can be found. 
    '''
    for d in rootDir.getDirectories():
        for f in d.getFiles():
            if f.getSource()[0] != os.path.sep and not f.getSource().startswith("http://") and not f.getSource().startswith("svn"):
                f.setSource(productSourceUrl + "/" + f.getSource())
        for cd in d.getDirectories():
            if cd.getSourceDir() != None and cd.getSourceDir()[0] != os.path.sep:
                cd.setSourceDir(productSourceUrl + "/" + cd.getSource())
    for f in rootDir.getFiles():
        if f.getSource()[0] != os.path.sep and not f.getSource().startswith("http://") and not f.getSource().startswith("svn"):
            f.setSource(productSourceUrl + "/" + f.getSource())
    for cd in rootDir.getDirectories():
        if cd.getSourceDir() != None and cd.getSourceDir()[0] != os.path.sep:
            cd.setSourceDir(productSourceUrl + "/" + cd.getSourceDir())



def getDefaultApplication(product):
    '''
        Reads the old style 'global' deployment tag :
            <product name="..">
                    <deployment>
                    </deployment>
                <application name="A">
                    ...
        
        OR the global application tag with its startscript(s), etc :
            <product name="..">
                <application>   <!-- alternatively : <application name="default"> -->
                    <deployment/>
                    <startscript/>
                <application>
                
        
    '''
    doc = libxml2.parseFile(product.getDeployConfig())
    productName = product.getName()
    defaultApp = None
    
    oldStyleDeploymentInfo = doc.xpathEval('//product[@name="%s"]/deployment' %productName)
    if len(oldStyleDeploymentInfo) == 1 :
        log.debug("Found %d old-style deployment tag(s)" %len(oldStyleDeploymentInfo))
        log.warn(tools.getDepricatedInfo("A <deployment> tag should always be in a <application>!"))
        defaultApp = Application(product, "default")
        depInfo = config.readDeploymentTag(defaultApp, oldStyleDeploymentInfo[0])
        defaultApp.setDeploymentInfo(depInfo)
        product.addApplication(defaultApp)

    # check the unamed application and with name='default' and read the defaultApp
    else :
        defaultAppTag = doc.xpathEval('//product[@name="' + productName + '"]/application[@name="default"]')
        if len(defaultAppTag) == 0: 
            defaultAppTag = doc.xpathEval('//product[@name="' + productName + '"]/application[not(@name)]')
    
        if len(defaultAppTag) > 0:
            log.debug("1. Loading global application tag.")
            defaultApp = config.readNormalApplicationTag(product, defaultAppTag[0])
            
            if len(defaultAppTag[0].xpathEval("startscript")) == 0:
                defaultScript = config.readJavaStartScript(defaultAppTag[0], defaultApp.getName(), None)
                defaultScript.setAppName("default")
                log.debug("Adding startscript '%s' to global app." %defaultScript.getProcessName())
                defaultApp.addStartScript(defaultScript)          
            
            
    if defaultApp == None :
        defaultApp = Application(product, "default")
    return defaultApp



        
def getApplicationsFromConfigOldStyle(product):

    if isinstance(product, Product) == False:
        raise Exception("passed parameter is not of Product class : %s" %product)

    log.debug("Reading applications for %s from %s" %(product.getName(), product.getDeployConfig()))
    doc = libxml2.parseFile(product.getDeployConfig())
    
    productName = product.getName()
    
    log.debug("Reading config content : \n%s" %(str(doc)))
    
    defaultApp = getDefaultApplication(product)
    product.addApplication(defaultApp)
           
    startscriptHandling = False
    if len(doc.xpathEval('//product[@name="' + productName + '"]/application/startscript')) > 0:
        startscriptHandling = True
           
    
    for appNode in doc.xpathEval('//product[@name="' + productName + '"]/application[@name]'):
        if config.getAttribute(appNode, 'name') == 'default':
            continue
            
        log.debug("3. Loading normal named application tag: \n" + str(appNode))
        
        toInherit = config.setDefaultIfNull(config.getAttribute(appNode, "extends"), "default")
        app = config.readNormalApplicationTag(product, appNode, product.getApplication(toInherit))
        
        if startscriptHandling == False:
            log.debug("We need to read the startscript info from the <application> directly...")
            parentApp = config.getAttribute(appNode, 'extends')
            parentScript = None
            if parentApp != None:
                log.debug("parentScript = " + parentApp)
                parentScript = product.getApplication(parentApp).getStartScripts()[0]
            else:
                log.debug("parentScript = default")
                if len(defaultApp.getStartScripts()) > 0: parentScript = defaultApp.getStartScripts()[0]
                
            script = config.readJavaStartScript(appNode, app.getName(), parentScript)
            script.setAppName(app.getName())
            #if script.getProcessName() == None:
            log.debug("Setting process name to " + app.getName())
            script.setProcessName(app.getName())
            app.addStartScript(script)
                
            if parentScript != None: app.removeStartScript(parentScript.getProcessName())
            
            
        app.setInstallDir("/opt/" + product.getName() + "/" + app.getName())
        if app.getDeploymentInfo().getInstallLocation() != None:
            log.debug("Overriding default installation location with : %s" %app.getDeploymentInfo().getInstallLocation())
            app.setInstallDir(app.getDeploymentInfo().getInstallLocation())
        
        for s in app.getStartScripts():
            if s.getAppName() == 'default': app.removeStartScript(s.getProcessName())
            if s.getProcessName() == 'default': app.removeStartScript(s.getProcessName())
            
        product.addApplication(app)
        

    # remove 'default' app, so it is not generated
    if product.hasApplication('default'):
        product.removeApplication('default')
    
    
    log.debug("Loaded Product \n" + str(product))
    return product
 
 
def getProductFromConfig(confFile, name):
    result = config.getProductsFromConfig(confFile)
    if result.has_key(name):
        return result[name]
    else:
        raise Exception("Cannot find product '%s' in '%s'" %(name, confFile))
    
     
     
     
class RepoInfo:
    #
    #
    #   
    def __init__(self, product):
        self.__productName = product
        self.__extractedXml = self.extractDataFromRepoXml() 
        #self.__readRepository()
        

    def extractDataFromRepoXml(self):
        repoFile = tempfile.mkstemp()
        #repoFile = open('/tmp/deploy.repo','w')
        #os.chmod( repoFile, string.atoi('0777',8) )
        log.debug('Processing global repository.xml and create product specific local one in ' + repoFile[1])
        os.close(repoFile[0])

        # copy to local disk and remove invalid chars as well as dependency information
        # this speeds up the processing - really!
        os.system('cp /user/pcrops/dist/repository.xml ' + repoFile[1])
        #os.system("perl -pe 's/[^[:ascii:]]//g; ' " + repoFile[1])
        os.system("sed -i '/<dep excluded=\|<dep product=/d' " + repoFile[1])

        log.debug('Getting copy and transforming repository.xml to extract only product relevant information.')
        f = open(repoFile[1], 'r')
        data = f.read()
        f.close()
        start = 0
        end = 0
        extracted = '<?xml version="1.0" encoding="UTF-8"?>\n<repository>'
    
        while start != -1:
                start = data.find('name="' + self.__productName + '" ', end)
                end = data.find('</product>', start)
                start2 = data.rfind('<product ', 0, start)
                extracted += data[start2:end + 10] + "\n"
        extracted += '</repository>'
        os.system("rm -f %s" %repoFile[1])
        return extracted

    def getAvailableVersions(self):
        myDoc = tools.readXmlFromString(self.__extractedXml)
        ret = []
        for product in myDoc.getElementsByTagName('product'):
            version = product.getAttribute("version")
            if product.getAttribute('link') != "":
                version += "(%s)" %product.getAttribute('link')
            ret.append(version)
        return ret

    def getProduct(self, version):
        prods = self.getAvailProducts()
        log.debug("Trying to find release info for %s and version %s" %(self.__productName, version))
        for product in prods:
            if product.getAliasVersion().find(version) >= 0 or product.getRealVersion() == version:
                return product
        raise Exception("Can't find for this product and version any information in the repository. ")   
    
    def getLatestRelease(self):
        return self.getProduct('PRO')
        
    def getAvailProducts(self):
        myDoc = tools.readXmlFromString(self.__extractedXml)
        ret = []
        for product in config.getChildNodes(myDoc, '//product[@name="%s"]' %(self.__productName)):
            name = product.hasProp('name').getContent()
            version = product.hasProp('version').getContent()
            aliasVersion = ""
            if product.hasProp('link') != None:
                aliasVersion = product.hasProp('link').getContent()
            
            if len(aliasVersion.split(",")) > 1:
                aliasVersion = aliasVersion.split(",")[0]
            
            # the repository.xml contains entries for the dev releases 
            # although they are not available anymore. we need to filter them out here:
            if version.startswith("dev.") and aliasVersion == "" : continue
            
            if aliasVersion == "" : aliasVersion = version
            sourceDir = '/user/pcrops/dist/%s/%s' %(product.hasProp('directory').getContent(), aliasVersion)
            deployConfig = os.path.join(sourceDir, 'product.xml')
            releaseDate = config.getChildNodes(product, 'releaseDate')[0].getContent()
            p = Product(name, sourceDir, aliasVersion=aliasVersion, realVersion=version, releaseDate=releaseDate)
            p.setDeployConfig(deployConfig)
            ret.append(p)
        return ret
        