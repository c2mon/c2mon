#!/usr/bin/python


'''
Deploy script for AP software
-----------------------------

	Usage : deploy [-c <product.xml>|-p <product>] -a <application> [OPTIONS]

	Where OPTIONS are:

	-p   The product's name as known by CommonBuild.
             e.g. -p myproduct

        -a   The application's name as stated in the 'application' tag of the product.xml
             e.g. -a myapp

        -v   The application's version to install.
             The version number should correspond to a directory in the repository (/user/pcrops/dist/product/<version>).
             Of course, this can also be PRO, NEXT or Qfix.       ! Default is PRO !

	-l   List available applications from the given configuration file.

        -t   The installation directory.
             This will override the location specified in the product.xml (installationLocation)

        -r   The source repository from where to fetch the files to install. Required in combination with -c option.

        -u   Update mode.
             Only update the libraries. For this, the argument must be a location where the product.xml can be found. 
             e.g. -u /opt/myproduct/. 
             Note: All files in lib will be removed

        -f   Force mode.
             This will definitly remove the old directory (if permissions are given), before installation.
	
	-s   Simulation mode.
	     This will only pretend to do the changes. No real ones are made.

        -d   Debug mode switch ON

	-c   The product configuration file location (product.xml). Useful when you want to test your product.xml before commiting.
	     e.g. : -c /user/pcrops/dist/accsoft/myproduct/PRO/product.xml


        -h   This text


author : F.Ehm, CERN 11/2009

'''

#
# First we import some libs
#
import sys
import getopt
import string
import tempfile, os, shutil
import re
import logging
import time
import smtplib
import socket
import fileinput
import filecmp
from email.MIMEText import MIMEText

import commands 

from xml.dom.ext.reader import Sax2
from xml.sax._exceptions import SAXParseException
reader = Sax2.Reader()

# currently only keeps the information that the default installation location
# should be overriden by the parameter value of --target option
#
override = {} 
override['installDir'] = ''

# 
# the main configuration list which is used for the installation
#
installConfig = {}
installConfig['mkdir'] = []
installConfig['cp'] = {}
installConfig['chmod'] = {}
installConfig['version'] = 'PRO'
installConfig['nbBackups'] = 1
installConfig['installLog'] = tempfile.mkstemp()
installConfig['forceMode'] = False 
installConfig['configFile'] = ''
installConfig['triggeringUser'] = ''
installConfig['product'] = ''
installConfig['application'] = ''
installConfig['rollback'] = 0
installConfig['hostname'] = socket.gethostname()
installConfig['simulationMode'] = False
installConfig['installDir'] = ''
installConfig['sourceDir'] = ''
installConfig['replacement'] = {}
installConfig['notification'] = []
installConfig['startscripts'] = []
installConfig['updateLibs'] = "" 
installConfig['showApps'] = False 
installConfig['localRepo'] = False 
installConfig['releaseDate'] = '' 
debugMode = 0
log = None

global install_log_file


#
# our XML doc as python object
# @used in readConfig
#
doc = None



#
# Reads the passed parameters, evaluates them and starts deploy()
#
def main():
	'''
	Reads given options and executes the deployment.
	'''
	global installConfig
	global debugMode
	configFile = None	# assume config is in current dir
	appName = None


	try:
		opts, args = getopt.getopt(sys.argv[1:], 'hp:c:v:dflst:u:a:r:' , ['help'])
	except getopt.error, msg:
		print msg
		print "for help use --help / -h"
		sys.exit(2)

	if len(sys.argv) == 1:
		print __doc__
		sys.exit(0)

	for o, a in opts:
		if o in ("-h", "--help"):
			print __doc__
			sys.exit(0)
		elif o in ('-a'):
			appName = a
		elif o in ('-p'):
			checkArg(a, '-p requires an argument')
			installConfig['product'] = a
		elif o in ('-c'):
			checkArg(a, '-a requires an argument')
			installConfig['configFile'] = a
		elif o in ('-t'):
			checkArg(a, '-t requires an argument')
			override['installDir'] = a + '/'
		elif o in ('-v'):
			checkArg(a, '-v requires an argument')
			installConfig['version'] = a
		elif o in ('-d'):
			debugMode = True
		elif o in ('-f'):
			installConfig['forceMode'] = True
		elif o in ('-l'):
                        installConfig['showApps'] = True
		elif o in ('-s'):
			installConfig['simulationMode'] = True
		elif o in ('-r'):
			if not os.path.exists(a):
				error("Path '" + a + "' does not exist!")
			installConfig['sourceDir'] = a
			installConfig['localRepo'] = True
			installConfig['version'] = 'LOCAL_BUILD'
			installConfig['releaseDate'] = 'LOCAL_BUILD'
		elif o in ('-u'):
			checkArg(a, '-u requires an argument')
			installConfig['updateLibs'] = a
		else:
			assert False, "No such option :" + o
	
	if installConfig['product'] != '' and installConfig['configFile'] != '':
		error("The '-p' and '-c' option exclude each other.")
		


	# we don't do a rollback, if we are in simulationMode
	if inSimulationMode():
		installConfig['rollback'] = False
		debugMode = True
		debug('Setting Rollback = False and debug = True, since we are in simulation mode')


	if installConfig['updateLibs'] != "" :
		try:
		# only update the libraries, take the specified product.xml
			installConfig['configFile'] = installConfig['updateLibs'] + "/product.xml"
			installConfig['installDir'] = installConfig['updateLibs']
			updateApplication()
		except Exception, e:
			error(str(e))
		sys.exit(0)
	

	# normal installation from here
	init()
	if installConfig['showApps'] == True:
		print showApplications()
		sys.exit(0)

	if appName == None :
		error ('Please specify an application you want to deploy. You can list them using -l')
	debug('APPNAME = ' + appName +', VERSION = ' + installConfig['version'])

#	try:
	deploy(appName)
#	except Exception, e:
#		error(str(e))
	
	sys.exit(0)


#
# Helper to list the available applications in the configuration file
#
def showApplications():
	global doc

	ret = ''
	for productNode in doc.getElementsByTagName('product'):
		ret += 'Available applications in config file for product ' + productNode.getAttribute('name') +' with version '+installConfig['version'] +':\n'
		for applicationNode in productNode.getElementsByTagName('application'):
			ret += applicationNode.getAttribute('name') + '\n'
	return ret 



def init():
	debug('Entering init()')
	global doc
	global installConfig
	repo = None

	# product name is given, try to find it in repo
	if installConfig['product'] != "":
		repo = RepoInfo(installConfig['product'], installConfig['version'])
		installConfig['configFile'] = repo.getSourceDir() + '/' + repo.getVersion() + '/' + 'product.xml'
		installConfig['releaseDate'] = repo.getReleaseDate()

	readConfig(installConfig['configFile'])

	# we'd like to have the releaseDate for the product version
	if installConfig['localRepo'] == False and repo == None:
		repo = RepoInfo(installConfig['product'], installConfig['version'])
		installConfig['releaseDate'] = repo.getReleaseDate()



#
# reads the deployment information from the configuration file and 
# triggers the installation
#
def deploy(appName):
	'''
	
	'''
	global doc
	global installConfig
	
	found = False 

	debug("Searching for " + appName)
	for product in doc.getElementsByTagName('product'):
		
		debug("Checking found product " + product.getAttribute('name'))

		# <application> is not optional if you want to install it
		for applicationNode in product.getElementsByTagName('application'):
			debug("Found application '" + applicationNode.getAttribute('name')+ "'")

			if applicationNode.getAttribute('name') == appName:
				productName = product.getAttribute('name')

				if len(installConfig['sourceDir']) == 0:
		                	installConfig['sourceDir'] = '/user/pcrops/dist/' + '/' + product.getAttribute('directory') + '/' + installConfig['version']
       				debug('Default source directory = ' + installConfig['sourceDir'])

				# <deployment> is optional
				deploymentTag = product.getElementsByTagName('deployment')
				if len(deploymentTag) > 0:
					readDeployment(deploymentTag[0])

				if installConfig['product'] != '':
					if productName != installConfig['product']:
						raise Exception("Application " + appName + " is not available for product " + installConfig['product'] + " but for " + productName)
				else:
					installConfig['product'] = productName

	       			findTriggeringUser()
				installConfig['application'] = appName
				readApplication(applicationNode)
				installApplication()
				found = True
				break
	if found == False:
		raise Exception("No information found in product.xml for application " + appName)
		
		

	


#
# reads <deployment> 
# @used in deploy()
#
def readDeployment( deploymentTag ):
	debug('Entering readDeployment()')
	global override
	global installConfig

	if deploymentTag == None:
		return

	debug('Found deployment informtion. Reading it...')

	# set the installation path only if nothing has been given to us 
	# as a cmd line  parameter to overrride
	tmp = deploymentTag.getAttribute('installLocation')
	if len(tmp) > 0 :
		installConfig['installDir'] = tmp + '/'

	debug('SOURCEDIR = ' + installConfig['sourceDir'])

	# now parse the deployment information
	for directory in deploymentTag.getElementsByTagName('directory'):
		# <deployment .. >
		targetDir = directory.getAttribute('name')
		debug('Found directory : ' + targetDir)
		targetDirMask = setDefaultIfNull(directory.getAttribute('mask'), '755')
		checkNotEmpty(targetDir, '"name" attribute for <directory> is empty.')
		
		installConfig['mkdir'].append(targetDir)

		for file in directory.getElementsByTagName('file'):
			# <file ../>
			fileName = file.getAttribute('name')
			checkNotEmpty(fileName, '"name" attribute for <file> is empty')
			debug('Found file :' + fileName)
			
			sourceFile = file.getAttribute('sourceDir')
			
			# in case refer to a file in the release directory we add the sourceDir before
			if sourceFile != None and len(sourceFile) > 0:
				if sourceFile[0] == '/':
					sourceFile = sourceFile + '/' + fileName
			else:
				sourceFile = installConfig['sourceDir'] + '/'+ fileName
			debug("Setting source to " + sourceFile) 
			checkAvailable(sourceFile)
			
			# we take the real file name. I.e. everything after '/'
			pos = fileName.rfind("/")
			if pos > 0:
				fileName = fileName[pos+1:len(fileName)]
			targetFile = "/".join([targetDir, fileName])
			mask = setDefaultIfNull(file.getAttribute('mask'), '644')
			# we have to make sure that we have the 4-digit chmod style
			installConfig['cp'][sourceFile] = targetFile
			if len(mask) == 3:
				mask = '0' + mask
			debug('Setting mask : '+ mask +' for ' + fileName)
			installConfig['chmod'][targetFile] = mask

	# now the backup policy
	if len(deploymentTag.getElementsByTagName('backupPolicy')) > 0:
		readBackupInformation ( deploymentTag.getElementsByTagName('backupPolicy')[0])
		
	# don't forget to read the notification, in case sombody is interested whenever the product is installed
	if len(deploymentTag.getElementsByTagName('notification')) > 0:
		readNotification( deploymentTag.getElementsByTagName('notification')[0] )
		
	if len(deploymentTag.getElementsByTagName('replaceString')) > 0:
		readStringReplacement( deploymentTag.getElementsByTagName('replaceString')[0] )

	debug('leaving readDeployment()')





#
# reads <application>, generates start script
# @used in deploy()
#
def readApplication( applicationNode ):
	global installConfig
	debug('Entering readApplication()')
	

	# read the required values
	name = applicationNode.getAttribute('name')

	# reading additional deployment info
	if len(applicationNode.getElementsByTagName('deployment')) > 0:
		readDeployment( applicationNode.getElementsByTagName('deployment')[0])

	# reset the installation Location in case it has changed.
	if not installConfig.has_key('installDir') or len(installConfig['installDir']) == 0:
		installConfig['installDir'] = '/opt/' + installConfig['product'] + '/' + name + '/'
	if len(override['installDir']) > 0:
		 installConfig['installDir'] = override['installDir']


	startscripts = applicationNode.getElementsByTagName('startscript')
	if len(startscripts) > 0:
		for startscript in startscripts:
			readStartscript( startscript )
	elif len(applicationNode.getElementsByTagName('main-class')) >0 :
		readStartscript( applicationNode )

	debug('Leaving readApplication()')



#
# reads <startscript and generates the startscript
#
def readStartscript( startscriptNode ):
	global installConfig
	debug('Entering readStartscript()')

	output = []
	main = startscriptNode.getElementsByTagName('main-class')[0].getAttribute('name')
	name = startscriptNode.getAttribute('name')
	checkNotEmpty(main, "No 'main-class' attribute for start script generation found.")
	checkNotEmpty(name, "No 'name' attribute for start script generation found.")

	# process name is UPPER case , if not given
        procName = setDefaultIfNull( startscriptNode.getAttribute('processName'),  name.upper() )
	debug('PROCNAME = ' + procName)
	
	# read JVM args, if given
	javaVmArg = readJavaVmArgs(startscriptNode)

	# read JVM Memory settings, if given
	javaVmMemorySettings = readJvmMemorySettings(startscriptNode)

	# generate the start script (bash version)
	output.append('#!/bin/bash')
	output.append('')
	output.append('# Generated shell start script for ' + name )
	output.append('')
	output.append('')
	output.append('[[ -z "$JAVA_HOME" ]] && export JAVA_HOME=/usr/java/jdk')
	output.append('export INSTALL_DIR=' + installConfig['installDir'])
	output.append("CLASSPATH=`ls $INSTALL_DIR/lib/*.jar | tr -s '\\n' ':'`")
	output.append('')
	output.append('')

	cmd = []
	cmd.append('exec -a "' + procName +'"')
	cmd.append('$JAVA_HOME/bin/java')
	cmd.append(readJavaVmArgs(startscriptNode))
	cmd.append(readJvmMemorySettings(startscriptNode))
        for property in startscriptNode.getElementsByTagName('property'):
	                 cmd.append('-D' + property.getAttribute('name') + '="' + property.getAttribute('value') + '"')
	cmd.append('-cp "$CLASSPATH"')
	cmd.append(main)
	for argument in startscriptNode.getElementsByTagName('main-class')[0].getElementsByTagName('arg'):
		cmd.append( argument.firstChild.nodeValue )

	output.append(" ".join(cmd))
	output.append('')

	# create the tmporary start script
	tmp = tempfile.mkstemp()
	os.write( tmp[0], "\n".join(output) )
	os.close( tmp[0] )
	debug('Adding startscript ' + tmp[1] + ' as ' + name )
	installConfig['startscripts'].append( [tmp[1], name])
	installConfig['chmod']['bin/' + name] = '0755'

	checkTransferRefForProc(procName)
	checkForRunningProc(procName)
	


#
# reads <initial-heap-size> and <max-heap-size>
# @used by readApplication()
#
def readJvmMemorySettings( applicationNode ):
	resultInitHeap = ""
	resultMaxHeap  = ""
	initHeap = getSafeTagValue( applicationNode, "initial-heap-size")
	maxHeap  = getSafeTagValue( applicationNode, "max-heap-size")

	if len(initHeap) > 0:
		if not re.match("\d{1,3}[M|G]", initHeap):
			error("The inital-heap-size value is not valid : " + initHeap)
		resultInitHeap = "-Xms" + initHeap
	if len(maxHeap) > 0:
		if not re.match("\d{1,3}[M|G]", maxHeap):
			error("The max-heap-size value is not valid : " + maxHeap)
		resultMaxHeap = "-Xmx" + maxHeap

	return resultInitHeap + " " + resultMaxHeap

#
# reads <java-vm-args>
# @used by readApplication()
#
def readJavaVmArgs ( applicationNode ):
	return getSafeTagValue( applicationNode , "java-vm-args")

# 
# reads the value of a tag and returns empty string if no value is found
#
def getSafeTagValue( applicationNode, tagName ):
	val = applicationNode.getElementsByTagName(tagName)
	if len(val) > 0:
		return val[0].firstChild.nodeValue
	return ""



#
# creates the file list which is used for initial installation and update 
# normally ONLY libs and some dirs
#
def createDefaultFileList():
	global installConfig
	# lets add the default stuff (dependencies, product.jar, product.xml, etc..)
        installConfig['mkdir'].append('lib')
        installConfig['mkdir'].append('bin')
	cp = installConfig['cp']
	sourceDir = os.path.normpath(installConfig['sourceDir']) + '/'

        for file in os.listdir( sourceDir + '/build/dist/' ):
                if re.match(".*\.jar", file):
                        cp[sourceDir + '/build/dist/' + file] = 'lib/' + file
        for dependency in os.listdir( sourceDir + '/lib'):
		if re.match(".*\.jar", dependency):
	                cp[sourceDir + '/lib/' + dependency] = 'lib/' + dependency


def checkProcessRunning(list):
	# Nothing
	return

# 
# creates the installation commands
# @used in deploy()
#
def installApplication():
	global installConfig

	installDir = os.path.normpath(installConfig['installDir']) + '/'
	installDir = os.path.expanduser(installDir);
	sourceDir = os.path.normpath(installConfig['sourceDir']) + '/'
	cp = installConfig['cp']
	mkdir = installConfig['mkdir']
	chmod = installConfig['chmod']

	createDefaultFileList()

	# our product.xml, we take it from the sourceDir
	cp[ sourceDir + '/' + 'product.xml' ] = 'product.xml'
	debug("")
	debug("")
	debug("Real installation log starts from here ..")
	debug("")
	installLog("---------------------------------------------------------------------------------------------------------------")

	installLog("Installation of application '%s' for product '%s'" %(installConfig['application'], installConfig['product']) )
	installLog('')
	installLog('Application             : ' + installConfig['application'])
	installLog('Product                 : ' + installConfig['product'])
	installLog('Version                 : ' + installConfig['version'])
	installLog('Release Date            : ' + installConfig['releaseDate'])
	installLog('Source                  : ' + sourceDir)
	installLog('Started on              : ' + time.strftime("%a, %d %b %Y %H:%M:%S ", time.localtime()) )
	installLog('Target directory is     : ' + installDir)
	installLog('Triggerd by             : ' + installConfig['triggeringUser'])
	installLog('Force installaton is    : ' + str(installConfig['forceMode']))
	installLog('Automatic rollback      : ' + str(installConfig['rollback']))
	installLog('Keeping backups         : ' + str(installConfig['nbBackups']))
	installLog('SimulationMode          : ' + str(installConfig['simulationMode']))
	installLog('') 
	installLog('Install log :') 


	# then do the deployment 
	try:

		# create (or not) the neccessary backups
		makeBackup()

		# force installation if wished
		if installConfig['forceMode'] :
			installLog('REMOVING ' + installDir)
			try:
				shutil.rmtree( installDir )
			except Exception, ex:
				# we do nothing
				installLog('No previous directory found')

		installLog('mkdir ' + installDir)
		if not inSimulationMode():
			os.makedirs( installDir )
		for directory in mkdir:
			installLog('mkdir ' + installDir + directory)
			if not inSimulationMode() :
				try:
					os.mkdir(installDir + directory)
				except OSError, e:
					if e[0] != 17:
						# the directory already exists
						pass

		for startscript in installConfig['startscripts'] :
			installLog('mv %s %s' %(startscript[0], installDir +'/bin/' + startscript[1]) )
			if not inSimulationMode() :
				shutil.move( startscript[0], installDir +'/bin/' + startscript[1] )
	
		# now copy files		
		for file in cp.keys():
			installLog('cp -p %s %s' %(file, installDir + cp[file]) )
			if not inSimulationMode() :
				shutil.copy2( file, installDir + cp[file] )

		# and do finally the replacements
		for file in installConfig['replacement'].items():
			installLog('Replacing in %s : placeholder = %s with new value = %s' %(installDir+file[0], file[1][0] ,file[1][1]) )
			if not inSimulationMode() :
				try: 
					replaceStringInFile( installDir + file[0], file[1][0], file[1][1] )
				except Exception,e:
					installLog(e[0])
					warning(e[0])

		# and chmod according to deployment info
		for file in chmod.keys():
			tmp = chmod[file]
			installLog('chmod %s %s' %(tmp, installDir + file))
			if not inSimulationMode() :
				os.chmod( installDir + file, string.atoi(tmp,8) )


		if not inSimulationMode() :
			try :
				sendNotification()
			except Exception, e:
				installLog('Cannot send notification : %s' %(str(e)))
				warning('Cannot send notification : %s' % (str(e)))
			shutil.copy2( installConfig['installLog'][1], installDir + 'install.log')
				
	except (ValueError, IOError, shutil.Error, OSError), e:
		installLog( str(e) )
		installLog( 'You can find the temporary logfile here : ' + installConfig['installLog'][1] )
		if installConfig['rollback']:
                        rollback()
		raise	
		




#
# Usaed to just update the libraries
#
def updateApplication():
	global installConfig
	installDir = installConfig['installDir']
	installDirLog = installDir + '/install.log'
	previousLog = ""


	try:
		debug("Reading previous install.log for source dir and version determination.")
		log = open(installDirLog, 'r')
		installConfig['sourceDir'] = ""
		# reading the log file
		for line in log:
			line = line.split(":")
			if line[0].strip() == "Source":
				installConfig['sourceDir'] = line[1].rstrip('\n').strip()
				debug("Source directory info found. Will copy libs from there")
			if line[0].strip() == "Version":
				installConfig['version'] = line[1].rstrip('\n').strip()
				debug("version info found : " + installConfig['version'])
			if line[0].strip() == "Product":
				installConfig['product'] = line[1].rstrip('\n').strip()
				debug("product info found : " + installConfig['product'])
		log.seek(0)
		previousLog = log.read();
		log.close()
	except Exception, e:
		error("Can't read from " + installDirLog + ": " +str(e))


	init()

	createDefaultFileList()	

	installLog('*********************************************************')
	installLog('Updating ' + installConfig['product'] + ' to version ' + installConfig['version'] )
	installLog('Release date : ' + installConfig['releaseDate'])
	installLog('')
	installLog('')


	if installConfig['sourceDir'] == "":
		error("Can't find any 'Source' information ")

	try:
		installLog('Removing old libraries ...')
		if not inSimulationMode():
			oldLibDir = installDir + '/lib_old'
			normalLibDir = installDir + '/lib'

			cp = installConfig['cp']
			if os.path.exists(oldLibDir):
				if installConfig['forceMode'] == True:
					installLog('Removing backup lib dir')
					shutil.rmtree(oldLibDir)
				else:
					error('Please remove previous library directory - its for your safety. \nYou can override this using the force mode option') 
				
			shutil.move(installDir + '/lib', installDir + '/lib_old')
			os.mkdir(installDir + '/lib') 
			debug('Copying new libraries ...')
			# now copy files
	                for file in cp.keys():
				target = installDir + '/' + cp[file]
				installLog('cp -p ' + file + ' ' + target)
	                        if not inSimulationMode() :
	                                shutil.copy2( file, target )
		installLog('Finshed.')
		installLog('*********************************************************')
		installLog('\n\n')
		# add installLog  to previous
		shutil.copy2( installConfig['installLog'][1], installDirLog)
		log = open(installDirLog,'a')
		log.write(previousLog)
		log.close()

	except (Exception, EnvironmentError), e:
		installLog( str(e) )
		pass
	
		

#
# read the <backup> tag
#
def readBackupInformation( backupNode ):
	global installConfig
	if backupNode == None:
		return
	debug('Found backupPolicy information')
	nbBackups =  backupNode.getAttribute('keepBackups')
	try:
		nbBackups = getIntOrFail(backupNode.getAttribute('keepBackups'))
	except Exception, e:
		raise Exception("Couldn't evaluate 'keepBackups' value : " + nbBackups)
	installConfig['nbBackups'] = nbBackups
	if nbBackups > 5 or nbBackups < 0:
 		raise Exception('Number of backups must be 0 <= x <= 5. Given : ' + str(nbBackups))
	

#
# reorganizes the backup directories
#
def makeBackup():
	global installConfig
	nbBackups = installConfig['nbBackups']
	if nbBackups == 0 :
		installLog('No backup requested.')
		return	# we do nothing
	installLog('Keeping max ' + str(nbBackups) + ' backups')
	
	installDir = os.path.normpath(installConfig['installDir'])
	if installDir.endswith('/'):
		installDir = installDir[:-1] 

	# remove oldest backup dir
	installLog('Rolling backup directories.')
	if os.path.exists(installDir + '.'+ str(nbBackups)):
		installLog('Removing oldest backup')
		installLog('rm -Rf ' + installDir + '.'+ str(nbBackups))
		if not inSimulationMode():
			shutil.rmtree( installDir + '.'+ str(nbBackups) )	
		
	# rename previous backup dirs (i.e. dir1 -> dir2, dir2 -> dir3
	for i in range ( nbBackups, 1, -1 ):
		if os.path.exists( installDir + '.' + str(i-1) ):
			installLog('mv ' + installDir + '.' + str(i-1) + ' ' + installDir + '.' + str(i))
			if not inSimulationMode():
				shutil.move( installDir + '.' + str(i-1) ,  installDir + '.' + str(i) )
	
	# move latest to <dir>.1
	if os.path.exists( installDir ):
		installLog('mv %s %s.1' %(installDir,installDir) )
		if not inSimulationMode():
			shutil.move( installDir, installDir + '.1' )



		
#
# reads the <notification> configuration
#
def readNotification( notificationNode ):
	global installConfig
	if notificationNode == None:
		return
	debug('Reading notification information')
	for item in notificationNode.getElementsByTagName('mail'):
		installConfig['notification'].append( item.getAttribute('address') )
#
# sends the installLog to mail recipients
#
def sendNotification():
	debug('Entering sendNotification()')
	global installConfig
	elements = installConfig['notification']
	if len(elements) == 0:
		return
	sender = 'deployment.process@%s.cern.ch' %(installConfig['hostname'])
	fp = open(installConfig['installLog'][1], 'r')
	msg = MIMEText(fp.read())
	fp.close()
	msg['Subject'] = '[DEPLOYMENT] A new instance of %s has been deployed' %(installConfig['application'])
	msg['From'] = sender
	msg['To'] =  ', '.join(elements)
	s = smtplib.SMTP()
	s.connect()
	s.sendmail(sender, elements , msg.as_string())
	s.close()


#
# reads the <replaceString> configuration
# @used by readDeployment
#
def readStringReplacement( replacementNode ):
	debug('Entering readStringReplacement()')
	global installConfig
	elements = replacementNode.getElementsByTagName('file')
	if len(elements) == 0:
		return

	debug('Reading string replacement information')
	for file in elements:
		fileName = file.getAttribute('name')
		var = file.getAttribute('var')
                value = file.getAttribute('value')
		my = (var,value)
		installConfig['replacement'][fileName] = my
		debug('Found replacement in %s : VAR=%s, NEW VALUE=%s' % (fileName,var,value) )
#
# replaces a string in a given file
# @used by installApplication
#
def replaceStringInFile( fileName, search, replace ):
	try :
		content = open (fileName, 'r').read()
		content = content.replace( search, replace )
		open(fileName, 'w').write(content)
	except Exception,e :
		raise Exception("Cannot replace String '" + search + "' in '" + fileName + "' : "+ str(e[1]))
	



#
# Helper for error messages
#
def error( msg ):
	out = '\nERROR ' + msg + '\n'
	sys.stderr.write(out)
	sys.exit(1)
#
# Helper to print debug messages
#
def debug( msg ):
	global debugMode
	if debugMode :
		print ('[ DEBUG ]   ' + msg)

#
# Helper to print debug messages
#
def info( msg ):
        print ('[ INFO ]   ' + msg)


#
# Helper to print warning messages
#
def warning ( msg ):
	sys.stderr.write( '[ WARNING ]   ' + msg +"\n" )

#
# Helper ot write the installation log file
#
def installLog( msg ):
	info( msg )
	os.write(installConfig['installLog'][0], msg +'\n')






#
# Helper to read the configuration file (i.e. product.xml)
#
def readConfig(configFile):
	'''
	Reads the XML config file
	'''
	debug('Entering readConfig()')
	global doc
	global installConfig

	if doc != None:
		return

	checkNotEmpty(configFile, 'No configuration file (product.xml) given.')
	try :
		debug('Reading config from ' + installConfig['configFile'])
		doc = reader.fromUri(installConfig['configFile'])
		#installConfig['product'] = doc.getElementsByTagName('product')[0].getAttribute('name')
	except Exception, ex :
		error ('Cannot read config file. ' + str(ex))


#
# Helper to set tup the logging facilities
#
def setupLogger():
	global log
	
	logging.basicConfig(filename=installConfig['installLog'], level=logging.DEBUG)
	log = logging.getLogger("deploy")
	ch = logging.StreamHandler()
	ch.setLevel(logging.DEBUG)
	formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
	ch.setFormatter(formatter)
	log.addHandler(ch)

#
# Checks is the first paremeter is empty of None
# and raises an Exception with errorMsg
#
def checkNotEmpty(value, errorMsg):
	if len(value) == 0:
		raise Exception(errorMsg)	
#
# Checks if var is unset and in case sets var to the passed default value
#
def setDefaultIfNull(var, defaultValue):
	if var == None or len(var) == 0:
		return defaultValue
	return var

#
# Helper for checking the CMD arguments
# @used in main()
#
def checkArg(arg, msg):
	if arg[0] == '-':
		error(msg)


#
# Evaluates who executes the deployment
#
def findTriggeringUser():
	debug('Entering findTriggeringUser()')
	global installConfig
	user = os.environ['USER']	
	u2 = os.environ['LOGNAME']
	u3 = os.getuid()
	if user == '':
		if u2 == '':
			user = u3
		else:
			user = u2
	installConfig['triggeringUser'] = user

#
# Helper which tries to convert the argument into a int
# and throws an Exception if it fails
#
def getIntOrFail( value ):
	try :
		return int(value)
	except ValueError, e:
		raise Exception( e[0] + value )
#
# Returns true in case we are in simulation mode 
#
def inSimulationMode():
	return installConfig['simulationMode']


#
#
#
def checkForRunningProc(procName):
	try:
		output = commands.getoutput("ps -f|grep -i " + procName +" | grep -v 'grep\|ps\|python'")
		proginfo = string.split(output)

		debug("checkForRunningProc : " + procName)
	except Exception, e:
		error("Problem in checkForRunningProc() : " + str(e))

#
#
#
def checkTransferRefForProc(procName):
	try:
		infile = open("/etc/transfer.ref","r")
		text = infile.read()
		infile.close()
		if text.find(procName) == -1:
			warning("Your process is NOT in the transfer.ref.")
	except Exception, e:
		warning("Can't read /etc/transfer.ref to check if '" + procName + "' exists. :" + str(e))



#
# checks if the passed file is readable
#
def checkAvailable(fileName):
	if not os.path.isfile(fileName):
		raise Exception("File '" + fileName + "' does not exist.")
	


class RepoInfo:
#
#
#
	repoData = None
	product = ""
	version = ""
	def __init__(self, product, version):
		self.product = product
		self.version = version
		self.readRepository( product, version)

	def readRepository(self, product, version):
		if self.repoData != None:
			return

		repoFile = tempfile.mkstemp()
		debug('Processing global repository.xml and create product specific local one in '+ repoFile[1])
	        os.close( repoFile[0] )

		os.system('cp /user/pcrops/dist/repository.xml ' + repoFile[1])
		os.system("perl -i.bk -pe 's/[^[:ascii:]]//g;' " + repoFile[1])

		self.debug('Trying to read from Repository using product='+product+' and version='+version)
		f = open(repoFile[1],'r')
		data = f.read()
		f.close()
		total = len(data)
		start=0
		end=0
		extracted = '<?xml version="1.0" encoding="UTF-8"?>\n<repository>'
	
		while start != -1:
	        	start = data.find('name="' + product + '"', end)
		        end = data.find('</product>',start)
		       	start2 = data.rfind('<product ', 0, start)
		        extracted += data[start2:end+10]+ "\n"
		extracted += '</repository>'
		doc = reader.fromString(extracted)
		#self.debug('Extracted XML : \n ' + extracted)

		for product in doc.getElementsByTagName('product'):
			if product.getAttribute('version').find(version) or product.getAttribute('link') == version:
				self.repoData = product
				break
		if self.repoData == None:
			raise Exception("Can't find for this product and version any information in the repository. Check '" + repoFile[1] + "' you see what I've found.")
		
	def getSourceDir(self):
		return '/user/pcrops/dist/' + self.repoData.getAttribute('directory')
	def getVersion(self):
		if self.version == 'NEXT':	
			return self.version
		return self.repoData.getAttribute('version')
	def getReleaseDate(self):
		tmp = self.repoData.getElementsByTagName('releaseDate')
		if len(tmp) > 0:
			return str(tmp[0].firstChild.data) 
	def debug(self, text):
		debug('RepoInfo : ' + text)
				


	


#
# for python in order to see what should be executed.
#
if __name__ == "__main__":
    main()
