#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Config::Properties;

##
# Definition of global variables
##
my $jardir = "../lib";
my $appdir = "tim2-jviews-viewer/";
my $codebase = "http://timweb.cern.ch/test/javaws";

#Reading version number from ../version.txt
open VFILE, "< ../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

# Reading property file ~/rep/c2mon/.c2mon.properties #
open PROPS, "< /user/timtest/rep/c2mon/client/c2mon-client.properties"
  or die "Unable to open configuration file /user/timtest/rep/c2mon/client/c2mon-client.properties";
my $c2monProperties = new Config::Properties();
$c2monProperties->load(*PROPS);
my $jdbcDriver = $c2monProperties->getProperty("jdbc.driver");
my $jdbcRoUrl = $c2monProperties->getProperty("jdbc.ro.url");
my $jdbcRoUser = $c2monProperties->getProperty("jdbc.ro.user");
my $jdbcRoPassword = $c2monProperties->getProperty("jdbc.ro.password");
my $jmsUrl = $c2monProperties->getProperty("jms.broker.url");
my $jmsUser = $c2monProperties->getProperty("jms.client.user");
my $jmsPassword = $c2monProperties->getProperty("jms.client.password");
close PROPS;

# Reading property file ../jms.properties #
open JMSPROPS, "< ../jms.properties"
  or die "Unable to open configuration file ../jms.properties";
my $jmsProperties = new Config::Properties();
$jmsProperties->load(*JMSPROPS);
my $jmsAdminMessageTopic = $jmsProperties->getProperty("jms.client.adminmessage.topic");
my $jmsSupervisionTopic = $jmsProperties->getProperty("jms.client.supervision.topic");
my $jmsHeartbeatTopic = $jmsProperties->getProperty("c2mon.jms.heartbeat.topic");
my $jmsRequestQueue = $jmsProperties->getProperty("jms.client.request.queue");
close JMSPROPS;


##
# Procedure to generate for each library defined in the ../lib directory
# an entry in the jnlp file.
#
sub jarlist {
	my $dir = shift;
	opendir DIR, $dir or return;
	my @contents = 
    		map "$dir/$_", 
    		sort grep !/^\.\.?$/,
    		readdir DIR;
  	closedir DIR;
  	foreach (@contents) {
              my $htmldir = $appdir.substr($_, 3, length($_));
    		if (!-l && -d) {
			&jarlist($_);
		}
		else {
 			if (/tim-jviews-viewer.jar$/) {
    				print "		<jar href=\"", $htmldir, "\" main=\"true\" download=\"eager\"/>\n";
			}
 			elsif (/jar$/) {
    				print "		<jar href=\"", $htmldir, "\" main=\"false\" download=\"eager\"/>\n";
			}
		}
    		next
  	}
}




##########################################
#         Generating JNLP file           #
##########################################

print "Content-type: application/x-java-jnlp-file" , "\n\n";
print "<?xml version = '1.0' encoding = 'utf-8'?>
	<jnlp spec=\"1.0+\"
	codebase=\"$codebase\"
	>
	<information>
		<title>TIM Viewer (TEST SERVER)</title>
	        <vendor>Technical Infrastructure Monitoring (TIM) Team</vendor>
	        <homepage href=\"tim-viewer/index.html\"/>
	        <description>The combined GTPM and synoptic viewer</description>
	        <icon kind=\"splash\" href=\"http://timweb.cern.ch/img/tim-animated-320x200.gif\"/>
	        <offline-allowed />
	</information>
	<security> 
		<all-permissions/> 
	</security> 
	<resources>
		<j2se version=\"1.6+\"  initial-heap-size=\"512M\" max-heap-size=\"512M\"/>" , "\n";

jarlist ("$jardir");

# Defines the version number that is shown in the TIM Viewer about dialog
print "		<property name=\"tim.version\" value=\"$viewerVersion\"/>\n";
# JMS configuration parameters needed by C2MON client API
print "		<property name=\"c2mon.jms.url\" value=\"$jmsUrl\"/>\n";
print "		<property name=\"c2mon.jms.user\" value=\"$jmsUser\"/>\n";
print "		<property name=\"c2mon.jms.passwd\" value=\"$jmsPassword\"/>\n";
print "   <property name=\"c2mon.client.jms.adminmessage.topic\" value=\"$jmsAdminMessageTopic\"/>\n";
print "		<property name=\"c2mon.client.jms.supervision.topic\" value=\"$jmsSupervisionTopic\"/>\n";
print "		<property name=\"c2mon.client.jms.heartbeat.topic\" value=\"$jmsHeartbeatTopic\"/>\n";
print "		<property name=\"c2mon.client.jms.request.queue\" value=\"$jmsRequestQueue\"/>\n";
# C2MON read-only credentials to STL database, needed for the history player and charts
print "		<property name=\"jdbc.driver\" value=\"$jdbcDriver\"/>\n";
print "		<property name=\"jdbc.ro.url\" value=\"$jdbcRoUrl\"/>\n";
print "		<property name=\"jdbc.ro.user\" value=\"$jdbcRoUser\"/>\n";
print "		<property name=\"jdbc.ro.password\" value=\"$jdbcRoPassword\"/>\n";

if (param('configurl')) {
	print "		<property name=\"configurationFilePath\" value=\"", param('configurl'), "\"/>", "\n";
}

print "	</resources>
	<resources os=\"Windows\" > 
		<property name=\"tim.log.file\" value=\"c:\\temp\\tim2-viewer.log\"/>
	</resources> 
	<application-desc main-class=\"ch.cern.tim.client.jviews.Main\">
	</application-desc>
</jnlp>" , "\n";
