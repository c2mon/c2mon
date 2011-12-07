#!/usr/bin/perl -wT
use lib '/user/alaser/perllib/Config-Properties-1.71/blib/lib';
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Config::Properties;

##
# Definition of global variables
##
my $jardir = "../lib";
my $appdir = "dmn2-dashboard-editor/";
# Default codebase points to operation
my $codebase = "http://cs-ccr-www1.cern.ch/~alaser"; 

#Reading version number from ../version.txt
open VFILE, "< ../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

# Reading property file ~/rep/c2mon/test/conf/.c2mon.properties #
open PROPS, "< /user/diamonop/c2mon/test/conf/.c2mon.properties"
  or die "Unable to open configuration file ~diamonop/c2mon/test/conf/.c2mon.properties";
my $c2monProperties = new Config::Properties();
$c2monProperties->load(*PROPS);
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
      if (/tim-dashboard-editor.jar$/) {
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
      		<title>DMN2 Viewer</title>
          <vendor>BE/CO-IN DIAMON Team</vendor>
      		<homepage href=\"tim-viewer/index.html\"/>
      		<description>This application is based on the IBM ILOG Dashboard Editor and allows users to draw dashboard diagrams for the TIM Viewer.</description>
			<icon href=\"http://timweb.cern.ch/img/tim-animated-320x200.gif\" kind=\"splash\"/>
      		<offline-allowed/>
		<shortcut online=\"false\">
		  <desktop/>
		  <menu submenu=\"TIM Dashboard Editor\"/>
		</shortcut>
	</information>
	<security> 
		<all-permissions/> 
	</security> 
	<resources>
		<java version=\"1.6+\" initial-heap-size=\"256M\"  max-heap-size=\"512M\"/>" , "\n";

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
print " </resources>
        <resources os=\"Windows\" >
                <property name=\"tim.log.file\" value=\"c:\\temp\\tim-dashboard-editor.log\"/>
        </resources>
        <application-desc main-class=\"ch.cern.tim.jviews.TimDashboardEditorMain\">
        </application-desc>
</jnlp>" , "\n";
