'''
    Library for the deploy tool.
    
    Contains functions to handle CommonBuild projects to be installed. 
    
    F.Ehm, CERN 2011  
'''


import os
import logging 
import tempfile
import time
import urllib

log = logging.getLogger('maven')

from common import Product
import tools
import config
import commonbuild

try:
    import json
except ImportError:
    import sys
    sys.path.append(os.path.join(os.path.dirname(__file__), "lib"))
    import simplejson as json 
import re

class Maven:
    '''
    
    Class to install or list Maven packages from Nexus repository.
    
    '''
    product = None
    
    myRepo = None
    
    repoArtifact = None

    def __init__(self, installConf):
        gav = installConf.getProductName()

        myRepo = MavenRepoinfo(gav)
        self.__installConfig = installConf
        
        if self.__installConfig.getSourceUrl() != None:
            log.debug("Setting to LOCAL BUILD for " + self.__installConfig.getSourceUrl())
            self.product = Product("LOCAL_BUILD", None, "LOCAL_BUILD", "LOCAL_BUILD")
            self.product.setSourceUrl(self.__installConfig.getSourceUrl())
        else:
            potentialPackages = None
            if self.__installConfig.getVersion() == None:
                # no version given, lets get the latest release (default)
                myRepo.getRepoInfo()
                potentialPackages = myRepo.getLatestRelease()
            else:
                # user has specified a version: lets try to find it.
                potentialPackages = myRepo.get(self.__installConfig.getVersion())
            log.debug("Got from Repo : " + str(potentialPackages))

            if potentialPackages == None or len(potentialPackages) == 0:
                raise Exception("This packages hasn't been released yet. Check if it is in the snapshot by using the '-l' option")
            if len(potentialPackages) > 1:
                raise Exception("I have found more than one package. Could you please be a bit more precise ?")
            self.repoArtifact = potentialPackages[0]
                
                
            installConf.setVersion(self.repoArtifact.getVersion())
            log.debug("Creating Maven project with input : %s,%s" % (str(self.repoArtifact), str(self.__installConfig)))
            
            if self.repoArtifact.isSnapshot():
                self.product = Product(self.repoArtifact.getGav(), None, "SNAPSHOT", realVersion=self.repoArtifact.getVersion())
            else:
                self.product = Product(self.repoArtifact.getGav(), None, "PRO", realVersion=self.repoArtifact.getVersion())
            self.product.setSourceUrl(self.repoArtifact.getResourceUrl())

        
    def install(self):
        '''
        TRIES to install the maven package using the information given by the InstallConfig object. 
        '''
        tmpdir = tempfile.mkdtemp()
        # contains info from the repository for this installation item
        artifact = self.repoArtifact
        # contains info on deployment, startscripts, notification, etc
        application = None

        log.debug("Using " + tmpdir + " as temporary directory")
        try:
            # we download the file reported by nexus (e.g. xxx.tar.gz) locally
            fileName = tools.handleGetFile(artifact.getResourceUrl(), tmpdir, "localFile")
            
            
            # we set the release as the timestamp of the file 
            self.product.setReleaseDate(time.ctime(os.path.getmtime(fileName)))
            
            
            # and we search in it for a deployment.xml:
            deployConfig = None
            try:
                deployConfig = tools.getFileFromTar(fileName, "deployment.xml", tmpdir)
            except KeyError:
                input = raw_input("No deployment info in tar found. Where do you want to install the tar ?")
                self.__installConfig.setInstallDir(input)

            log.debug("Getting install instructions for " + artifact.getArtifact())
            if deployConfig == None:
                log.info("No deployment config found. Using default one.")
                from common import Application
                application = Application(self.product, 'default')
                self.product.addApplication(application)
            else:
                # product name not mandatory in the maven deployment xml. thats why add this on the fly here.
                fixProductNameInConfig(deployConfig, self.__installConfig.getProductName())
                

                self.product = config.getApplicationsFromConfig(deployConfig, self.product, self.__installConfig.getApplicationName())
                log.debug("Loaded Applications : \n" + str(self.product))
                
                apps = self.product.getApplications()
                if len(apps) > 1 and self.__installConfig.getApplicationName() == None:
                    # ask the user for the application to install (nothing specified via cmd option '-a')
                    while application == None:
                        try:
                            for a in apps: print a.getName()
                            input = raw_input(tools.red("I have found more than one deployment config. Please tell me which one I should use.: "))
                            application = self.product.getApplication(input)
                        except KeyError:
                            pass
                elif len(apps) == 0:
                    raise Exception("I couldn't find product '%s' in the deployment.xml. Please check if you have it named there." %self.product.getName())
                elif len(apps) == 1:
                    application = apps[0]
                else:
                    # we were given a name which we need to find in the load apps
                    application = self.product.getApplication(self.__installConfig.getApplicationName())
                    if application == None:
                        raise Exception("I can't find " + self.__installConfig.getApplicationName() + " available." )

                    
            if (self.__installConfig.getInstallDir() != None):
                application.setInstallDir(self.__installConfig.getInstallDir())

            # read the changelog (if given)
            try:
                self.product.setChangeLog(tools.readChangelog(tools.getFileFromTar(fileName, "changelog.txt", tmpdir)))
            except KeyError, ex:
                # IGNORE : means there is not changelog in the tar.gz
                pass

            tools.doPreInstall(application, self.__installConfig)

            tools.extractTar(fileName, application.getInstallDir())

            tools.doPostInstall(application, self.__installConfig)

        finally:
            log.debug("CLEANUP")
            os.system("rm -Rf " + tmpdir)


def fixProductNameInConfig(deployConfig, name):
    import libxml2
    import os
    os.chmod(deployConfig, 0755)
    doc = libxml2.parseFile(deployConfig)
    p = doc.xpathEval('//product')[0]
    p.setProp('name', name)
    if doc.saveFile(deployConfig) < 0:
        raise Exception("Couldn't fix the name in " + deployConfig)
        


class MavenRepoinfo(object):
    '''
        Internal class to fetch information from the NEXUS repository. 
    '''
    def __init__(self, name):
        groupId = None
        artifact = None
        classifier = None
        extension = None

        if name.count(':') >= 1:
            groupId = name.split(":")[0]
            artifact = name.split(":")[1]
            tools.checkNotEmpty(artifact,"Invalid name for artifact. Format is groupId:artifact[:classifier[:extension]]")
            if name.count(':') >= 2:
                classifier = name.split(":")[2]
                tools.checkNotEmpty(classifier,"Invalid name for classifier. Format is groupId:artifact[:classifier[:extension]]")
            if name.count(':') == 3:
                extension = name.split(":")[3]
                tools.checkNotEmpty(extension ,"Invalid name for classifier. Format is groupId:artifact[:classifier[:extension]]")
        else:
            groupId = name
        self.__init(groupId, artifact, classifier, extension)
        
    def __init(self, groupId, artifact, classifier, extension = None):
        self.__groupId = groupId
        self.__artifact = artifact
        self.__classifier = classifier
        self.__extention  = extension
        self.__jsonData = None
        self.__doc = None
        
    def getGroupId(self):
        return self.__groupId
    def getArtifact(self):
        return self.__artifact
    def getClassifier(self):
        return self.__classifier
    def getExtention(self):
        return self.__extention

    # "http://artifactory/api/search/gavc?g=cern.accsoft.commons&a=accsoft-commons-util"
    def getRepoInfo(self):
        log.debug("Building Repo data using : %s,%s,%s"  %(self.__groupId,self.__artifact,self.__classifier))
        
        url = "http://artifactory/api/search/gavc?g=%s" % (self.__groupId)
        if self.__artifact != None:
            url = "%s&a=%s" %(url, self.__artifact)
        if self.__classifier != None:
            url = "%s&c=%s" %(url, self.__classifier)
        
        log.debug("Getting Repo info from " + url)

        self.__jsonData = self.__getJsonFromUrl(url)
        log.debug(self.__jsonData)
        self.__doc = json.loads(self.__jsonData)
        self.__checkResult()

    def __getJsonFromUrl(self, url):
        f = urllib.urlopen(url)
        JsonData = f.read()
        f.close()
        return JsonData
        
    def __checkResult(self):
        if self.__jsonData == None:
            raise Exception("Got no data from repository ")
        if not json.loads(self.__jsonData)['results']:
            raise Exception("Got no data from repository, please check the provided gav ")
#   resultSize = self.__doc.xpathEval('//totalCount')[0].getContent()
#       resultExceed = self.__doc.xpathEval('//tooManyResults')[0].getContent()
#        if resultSize == 0:
#            raise Exception("Can't find a such group in artifacts.")
#        if resultExceed == "true":
#            raise Exception("Exceeded possible request size! Please reduce search criteria.")
            
            
    '''
        Sophisticated parsing to retrieve the different elements (version, repo, classifier, packaging)
    '''
    def getAvailVersions(self):
        artifactoryBaseApiUrl = "http://artifactory/api/storage/"
        artifactoryDevRepoUrl = "http://artifactory/development/"
        packages = []
        if self.__doc == None:
            return []
        list = self.__doc['results']
        entry = 1
        for a in list:
            log.debug("Checking entry %s"  %(entry))
            uri = a['uri']
            # Remove the baseApiUrl
            #beco-development-local/cern/dmn2/dmn2-daq/dmn2-daq-deploy/1.0.32-SNAPSHOT/dmn2-daq-deploy-1.0.32-20130227.110823-2.pom
            #beco-release-local/cern/dmn2/dmn2-daq/dmn2-daq-deploy/1.0.19/dmn2-daq-deploy-1.0.19-assembly.tar.gz
            filteredUrl = uri.replace(artifactoryBaseApiUrl, "")
            # Remove the groupId
            #beco-release-local/dmn2-daq-deploy/1.0.19/dmn2-daq-deploy-1.0.19-assembly.tar.gz
            filteredUrl = filteredUrl.replace(self.__groupId.replace(".", "/") + "/", "")
            # remove the artifactId
            #beco-release-local/1.0.19/dmn2-daq-deploy-1.0.19-assembly.tar.gz
            filteredUrl = filteredUrl.replace(self.__artifact + "/", "")
            filtersplit = filteredUrl.split("/")
            
            version = filtersplit[1]
            repo = filtersplit[0]
            
            # Remove repo
            #1.0.19/dmn2-daq-deploy-1.0.19-assembly.tar.gz
            filteredUrl = filteredUrl.replace(repo +"/", "")
            
            # Remove the version
            #dmn2-daq-deploy-1.0.19-assembly.tar.gz
            filteredUrl = filteredUrl.replace(version + "/", "")
            
            # Remove the artifactId
            #1.0.19-assembly.tar.gz
            #1.0.32-20130227.110823-2.pom
            #1.0.32-20130304.143525-4-assembly.tar.gz
            filteredUrl = filteredUrl.replace(self.__artifact + "-", "")
            
            classifier = ""
            # isolate the packaging and the classifier
            # analyze from the end of the string
            if "SNAPSHOT" in version:
                vsplit = filteredUrl.split(".")
                reachedVersion = False
                index = -1
                stringBuffer = ""
                while not reachedVersion:
                    current = vsplit[index]
                    if not "-" in current:
                        if stringBuffer:
                            stringBuffer = "." + stringBuffer
                        stringBuffer = current + stringBuffer
                        index -= 1
                    else:
                        if not re.match("^[0-9-]+$", current):
                            currentSplit = current.split("-")
                            classifier = currentSplit[-1]
                        reachedVersion = True
                packaging = stringBuffer
                
            else:
                #-assembly.tar.gz
                UrlTail = filteredUrl.replace(version, "")
                vsplit = UrlTail.split(".")
                reachedVersion = False
                index = -1
                stringBuffer = ""
                try:
                    while not reachedVersion:
                        current = vsplit[index]
                        if not "-" in current:
                            if stringBuffer and current:
                                stringBuffer = "." + stringBuffer
                            stringBuffer = current + stringBuffer
                            index -= 1
                        else:
                            classifier = current.replace("-", "")
                            reachedVersion = True
                except IndexError:
                    log.debug("No classifier")
                packaging = stringBuffer
                
            # Adjusting download URL
            resourceUrl = artifactoryDevRepoUrl + self.__groupId.replace(".", "/") + "/" + self.__artifact + "/" + version + "/" + self.__artifact + "-" + version
            if classifier:
                resourceUrl += "-" + classifier
            resourceUrl += "." + packaging
                
            p = None
            # filteredUrl by extension
            if self.getExtention() != None:
                if packaging == self.getExtention():
                    p = MavenPackage(self.__groupId, self.__artifact, classifier, resourceUrl, packaging, repo, version)
            else:
                p = MavenPackage(self.__groupId, self.__artifact, classifier, resourceUrl, packaging, repo, version)
               
            log.debug("Found entry %s", p)
        # Adding war packaging for taking the c2mon-web-configviewer
            if p.getPackaging() != 'tar.gz' and p.getPackaging() != 'tgz' and p.getPackaging() != 'zip' and p.getPackaging() != 'war':
                continue
 
            if p != None:
                #Avoid duplicates of SNAPSHOT versions
                # filter out the pom, nexus was not giving those artifacts
                if not p in packages and p.getPackaging() != "pom" and p.getPackaging() != "jar": 
                    packages.append(p)
                    entry = entry + 1
                
        sorted_packages = sorted(packages,key=lambda package: (package.getArtifact(),package.getClassifier(),package.getVersion()))
        return sorted_packages

    def getLatestRelease(self, searchForSnapShot = False):
        '''
        Searches the Nexus Repo and tries to find the latest release. If the passed argument is True
        only SNAPSHOTS are searched.
        
        @return: A list with the found MavenPackage(s)
        
        '''
        log.debug("Getting latest release. SNAPSHOT search is " + str(searchForSnapShot))
        result = []
        
        # filter from all available only the SNAPSHOTS
        filterPackages = []
        for p in self.getAvailVersions():
            if p.isSnapshot() == True:
                if searchForSnapShot == True:
                    filterPackages.append(p)
            else:
                filterPackages.append(p)
        #byVersion = sorted(filterPackages, key=lambda package: package.getVersion(), reverse=True)
        from distutils import version
        byVersion = sorted(filterPackages, key=lambda package: version.LooseVersion(package.getVersion()), reverse=True)
        
        # now filter all out which have the same (newest) SNAPSHOT version 
        if len(byVersion) > 1:
            # latest version on top of list
            snapshotStringVersion = byVersion[0].getVersion()
            result.append(byVersion[0])
            log.debug("Searching for other packages with same version " + snapshotStringVersion)
            
            # find other with same version
            # may happen when no artifact or classifier has been specified
            for package in byVersion[1:]:
                if package.getVersion() == snapshotStringVersion:
                    result.append(package)
        else:
            result = byVersion
        
        if not isinstance(result, list):
            raise Exception("Search got me not an array back. Please contact the developer!")
        
        return result
        
    
    def getLatestReleaseUrl(self):
       self.getRepoInfo()
       result = []
       
       result = self.getLatestRelease()
           
       if len(result) == 0:
            raise Exception("No release found for version " + version)
       if len(result) > 1:
            s = ""
            for a in result: s += str(a) +"\n"
            raise Exception("More than one release found for version %s.\n%s\n Please specify a classifier."  %(version,s))
           
       return result[0].getResourceUrl()
       
    def getLatestSnapshotUrl(self):
       self.getRepoInfo()
       result = []
       
       result = self.getLatestRelease(True)
           
       if len(result) == 0:
            raise Exception("No Snapshot found for version " + version)
       if len(result) > 1:
            s = ""
            for a in result: s += str(a) +"\n"
            raise Exception("More than one Snapshot found for version %s.\n%s\n Please specify a classifier."  %(version,s))
           
       return result[0].getResourceUrl()
        

    
    def get(self, version):
        '''
        Returns a 
        '''
        self.getRepoInfo()
        potentialArtifacts = []
        
        if version == 'SNAPSHOT':
            potentialArtifacts = self.getLatestRelease(True)
        else:    
            for p in self.getAvailVersions():
                if p.getVersion() == version and p.getGroupId() == self.__groupId and p.getArtifact() == self.__artifact:
                    potentialArtifacts.append(p)
                    
        if len(potentialArtifacts) == 0:
            raise Exception("No artifact found for version " + version)
        if len(potentialArtifacts) > 1:
            s = ""
            for a in potentialArtifacts: s += str(a) +"\n"
            raise Exception("More than one artifact found for version %s.\n%s\n Please specify a classifier."  %(version,s))
            
        return potentialArtifacts
    
    

class MavenPackage(object):
    def __init__(self, groupId, artifact, classifier, resourceUrl, packaging, repo, version):
        self.__groupId = groupId
        self.__artifact = artifact
        self.__classifier = classifier
        self.__resourceUrl = resourceUrl
        self.__packaging = packaging
        self.__version = version
        self.__repo = repo
    def getGroupId(self):
        return self.__groupId
    def getArtifact(self):
        return self.__artifact
    def getClassifier(self):
        return self.__classifier
    def getResourceUrl(self):
        return self.__resourceUrl
    def getPackaging(self):
        return self.__packaging
    def getVersion(self):
        return self.__version
    def getRepo(self):
        return self.__repo
    def getGav(self):
        ret = self.__groupId + ':' + self.__artifact
        if self.__classifier != None:
            ret = ret + ':' + self.__classifier
        return ret
    def isSnapshot(self):
        if self.getVersion().upper().find("SNAPSHOT") > 0:
            return True
        else:
            return False
    def __str__(self):
        return "%-30s %-30s %-8s %-15s %-8s %-8s" %(self.__groupId,self.__artifact,self.__classifier,self.__version,self.__packaging, self.__repo)
    
    def __eq__(self, other): 
        return self.__groupId == other.__groupId and self.__artifact == other.__artifact and self.__classifier == other.__classifier and self.__packaging == other.__packaging and self.__version == other.__version and self.__repo == other.__repo









