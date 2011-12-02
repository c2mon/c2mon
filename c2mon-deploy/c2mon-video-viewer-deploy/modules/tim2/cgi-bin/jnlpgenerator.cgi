#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Config::Properties;

##
# Definition of global variables
##
my $jardir = "../lib";
my $appdir = "tim2-video-viewer/";
my $codebase = "http://timweb.cern.ch/test/javaws";

#Reading version number from ../version.txt
open VFILE, "< ../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

# Reading property file ~/rep/c2mon/.c2mon.properties #
open PROPS, "< /user/timoper/rep/c2mon/client/c2mon-client.properties"
  or die "Unable to open configuration file /user/timoper/rep/c2mon/client/c2mon-client.properties";
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
      if (/tim-video-viewer.jar$/) {
            print "   <jar href=\"", $htmldir, "\" main=\"true\" download=\"eager\"/>\n";
      }
      elsif (/jar$/) {
            print "   <jar href=\"", $htmldir, "\" main=\"false\" download=\"eager\"/>\n";
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
	<jnlp spec=\"1.0+\" codebase=\"$codebase\">
	<information>
      		<title>TIM Video Viewer</title>
      		<vendor>Technical Infrastructure Monitoring (TIM) Team</vendor>
      		<homepage href=\"http://timweb.cern.ch\"/>
      		<description>Used in CCC to monitor the access to the tunnels</description>
		<icon href=\"http://timweb.cern.ch/img/tim-video-splash.gif\" kind=\"splash\"/>
      		<offline-allowed/>
		<shortcut online=\"false\">
		  <desktop/>
		  <menu submenu=\"TIM Video Viewer\"/>
		</shortcut>
	</information>
	<security> 
		<all-permissions/> 
	</security> 
	<resources>
		<java version=\"1.6+\" initial-heap-size=\"128M\"  max-heap-size=\"512M\"/>" , "\n";

jarlist ("$jardir");

# Defines the version number that is shown in the TIM Viewer about dialog
print "   <property name=\"tim.version\" value=\"$viewerVersion\"/>\n";
# JMS configuration parameters needed by C2MON client API
print "   <property name=\"c2mon.jms.url\" value=\"$jmsUrl\"/>\n";
print "   <property name=\"c2mon.jms.user\" value=\"$jmsUser\"/>\n";
print "   <property name=\"c2mon.jms.passwd\" value=\"$jmsPassword\"/>\n";
print "   <property name=\"c2mon.client.jms.supervision.topic\" value=\"$jmsSupervisionTopic\"/>\n";
print "   <property name=\"c2mon.client.jms.heartbeat.topic\" value=\"$jmsHeartbeatTopic\"/>\n";
print "   <property name=\"c2mon.client.jms.request.queue\" value=\"$jmsRequestQueue\"/>\n";

if (param('configurl')) {
	print "		<property name=\"configurationFilePath\" value=\"", param('configurl'), "\"/>", "\n";
}

print "	</resources>
	<application-desc main-class=\"cern.c2mon.client.video.TimVideoViewer\">
	</application-desc>
</jnlp>" , "\n";
