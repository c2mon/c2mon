#!/usr/bin/python

''' 
C2MON ISO creation script
Author: Nacho Vilches
'''


###########################
#       Imports           #
###########################
import urllib
import tarfile
import os
import shutil
import maven
from optparse import OptionParser

###########################
#       Definitions        #
###########################

##
# Creates a directory
#
def mkdir(dir):
    try:
        if os.path.exists(dir):
          shutil.rmtree(dir)
        os.makedirs(dir)
    except OSError, e:
        if e[0] != 17:
            raise
            
##
# Gets repo information for a given repo
#
def getRepoInfo(repo):
  r = maven.MavenRepoinfo(repo)
  r.getRepoInfo()
  
  print "REPO INFO :"
  for p in r.getAvailVersions():
    print p
   
##
# Gets release information for a given repo
#
def getLatestReleaseInfo(repo):
  r = maven.MavenRepoinfo(repo)
  r.getRepoInfo()
    
  print "LATEST RELEASE INFO :"
  for p in r.getLatestRelease():
      print p
  
##
# Gets snapshot information for a given repo
#
def getLatestSnapshotInfo(repo):
  r = maven.MavenRepoinfo(repo)
  r.getRepoInfo()
  
  print "LATEST SNAPSHOT INFO :"
  for p in r.getLatestRelease(True):
   print p
  
##
# Gets latest release for a given repo
#
def getLatestRelease(repo):
  r = maven.MavenRepoinfo(repo)
  r.getRepoInfo()
  
  print "LATEST RELEASE :"
  for p in r.getLatestRelease():
      print r.getLatestReleaseUrl()
  
  return r.getLatestReleaseUrl()
  
##
# Gets latest snapshot for a given repo
#
def getLatestSnapshot(repo):
  r = maven.MavenRepoinfo(repo)
  r.getRepoInfo()
  
  print "LATEST SNAPSHOT :"
  for p in r.getLatestRelease(True):
   print r.getLatestSnapshotUrl()
  
  return r.getLatestSnapshotUrl()

##
# Defining the parsing options
#
parser = OptionParser()
parser.add_option("-s", "--snapshot",
                  action = "store_true", dest = "isAsking4Snapshot", default = False,
                  help = "take the lates snapshop version for daq, server and web viewer")
(options, args) = parser.parse_args()


###########################
#       Main Routine      #
###########################

#getLatestSnapshotInfo("cern.c2mon.c2mon-deploy:c2mon-daq-testhandler-demo-deploy")
#getLatestSnapshotInfo("cern.c2mon.c2mon-deploy:c2mon-srv-tim2")


# Create the directories for server
print "Creating directories"
mkdir("demo")
os.chdir("demo")

###########################
#       Server      
##

# Create the directories for server
print "Creating server directories"
mkdir("server")
os.chdir("server")

print "Downloading server"
if (options.isAsking4Snapshot):
  urllib.urlretrieve(getLatestSnapshot("cern.c2mon.c2mon-deploy:c2mon-srv-demo"), "server.demo.tar.gz")
else:
  urllib.urlretrieve(getLatestRelease("cern.c2mon.c2mon-deploy:c2mon-srv-demo"), "server.demo.tar.gz")
 
# open the tarfile and use the 'r:gz' parameter
tfile = tarfile.open("server.demo.tar.gz", 'r:gz')
 
# extract all the contents of the archive
print "Extracting Server tarball"
tfile.extractall('.')
os.remove("server.demo.tar.gz")

print "Done"

# Come back to demo/
os.chdir("../")

###########################
#       DAQ      
##

# Create the directories for daq
print "Creating DAQ directories"
mkdir("daq")
os.chdir("daq")

print "Downloading DAQs"
if (options.isAsking4Snapshot):
  urllib.urlretrieve(getLatestSnapshot("cern.c2mon.c2mon-deploy:c2mon-daq-testhandler-demo-deploy"), "daq.testhandler.demo.tar.gz")
else:
  urllib.urlretrieve(getLatestRelease("cern.c2mon.c2mon-deploy:c2mon-daq-testhandler-demo-deploy"), "daq.testhandler.demo.tar.gz")
 
# open the tarfile and use the 'r:gz' parameter
tfile = tarfile.open("daq.testhandler.demo.tar.gz", 'r:gz')
 
# extract all the contents of the archive
print "Extracting DAQ tarball"
tfile.extractall('.')
os.remove("daq.testhandler.demo.tar.gz")
print "Done"

# Come back to demo/
os.chdir("../")

###########################
#       Tomcat      
##

# Create the directories for Tomcat
print "Creating Tomcat server directories"

# Copy tomcat server for c2mon-web-configviewer
shutil.copy2('/user/timadm/dist/rep/tomcat/apache-tomcat.tar.gz', 'apache-tomcat.tar.gz')

# open the tarfile and use the 'r:gz' parameter
tfile = tarfile.open("apache-tomcat.tar.gz", 'r:gz')
 
# extract all the contents of the archive
print "Extracting Tomcat server tarball"
tfile.extractall('.')
os.remove("apache-tomcat.tar.gz")

# renaming directory "apache-tomcat-'version'" to "apache-tomcat-main"
#print os.listdir(os.getcwd())
for fn in os.listdir(os.getcwd()):
  if 'tomcat' in fn:
    print "Renaming %s to apache-tomcat-main" %fn
    shutil.move(fn,"apache-tomcat-main")

# add c2mon-web-configviewer.war
print "Downloading c2mon-web-configviewer.war"
os.chdir("apache-tomcat-main/webapps")
if (options.isAsking4Snapshot):
  urllib.urlretrieve(getLatestSnapshot("cern.c2mon.c2mon-client:c2mon-web-configviewer"), "c2mon-web-configviewer.war")
else:
  urllib.urlretrieve(getLatestRelease("cern.c2mon.c2mon-client:c2mon-web-configviewer"), "c2mon-web-configviewer.war")

# change the applicationContext-security.xml with rbac removed
print "Replacing context-security.xml for no rbac config"
#os.system('unzip c2mon-web-configviewer.war -d c2mon-web-configviewer')
#os.system('jar -cvf c2mon-web-configviewer.war c2mon-web-configviewer')
mkdir("WEB-INF")
mkdir("WEB-INF/conf")
shutil.copy2('/user/timadm/dist/rep/tomcat/demo/webapps/c2mon-web-configviewer/WEB-INF/context-security.xml', 'WEB-INF/conf/context-security.xml')

# replace the datasource with the hsqldb source
#print "Replacing context-datasource.xml for hsqldb datasource"
#shutil.copy2('../../server/conf/c2mon-web-configviewer-datasource.xml', 'WEB-INF/conf/context-datasource.xml')

os.system('zip -r c2mon-web-configviewer.war WEB-INF/conf/context-security.xml')
#os.system('zip -r c2mon-web-configviewer.war WEB-INF/conf/context-datasource.xml')
#shutil.rmtree('c2mon-web-configviewer')

# add start-tomcat-demo.sh with special config for starting demo 
print "Downloading start-tomcat-demo.sh"
os.chdir("../bin")
shutil.copy2('/user/timadm/dist/rep/tomcat/demo/bin/start-tomcat-demo.sh', 'start-tomcat.sh')

# Download c2mon-client-demo.properties from the web
print "Downloading c2mon-client-demo.properties"
os.chdir("../conf")
urllib.urlretrieve ("http://timweb/test/conf/c2mon-client-demo.properties", "c2mon-client.properties")

print "Copying c2mon-web.properties"
shutil.copy2('/user/timadm/dist/rep/tomcat/demo/conf/c2mon-web.properties', 'c2mon-web.properties')

print "Copying chart-config.xml"
shutil.copy2('/user/timadm/dist/rep/tomcat/demo/conf/chart-config.xml', 'chart-config.xml')

print "Done"

# Come back to demo/
os.chdir("../../")

###########################
#       Manual      
##

# Download C2MON manual from edms
print "Downloading C2MON manual from edms"
urllib.urlretrieve ("https://edms.cern.ch/file/1221592/3/C2MON-manual.pdf", "C2MON-manual.pdf")
print "Done"

# Download C2MON-Demo manual from edms
print "Downloading C2MON-Demo manual from edms"
urllib.urlretrieve ("https://edms.cern.ch/file/1399049/1/C2MON-demo-manual.pdf", "C2MON-demo-manual.pdf")
print "Done"

# Come back to root directory
os.chdir("../")

###########################
#       tarball      
##
print 'Creating tarball'
tar = tarfile.open("c2mon-demo.tar.gz", "w:gz")
tar.add('demo', arcname='c2mon-demo')
tar.close()
print 'Done'


###########################
#       ISO      
##

# print "Creating iso"
# os.system("mkisofs -r -o demo.iso 'demo'")
# 
# 
# print "Cleaning directory"
#shutil.rmtree('demo')
 
print "Done! Have a nice day."


