#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Config::Properties;

##
# Definition of global variables
#
my $jardir                    = "../lib";
my $appdir                    = "tim2-jviews-viewer";
my $codebase                  = "http://timweb/javaws";
my $c2monClientPropertiesURL  = "http://timweb/conf/c2mon-client.properties";
my $log4jURL                  = "http://timweb/conf/log4j.xml";

##
# Reading version number from ../version.txt
#
open VFILE, "< ../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

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
              my $htmldir = substr($_, 3, length($_));
    		if (!-l && -d) {
			&jarlist($_);
		}
		else {
 			if (/tim-jviews-viewer/) {
    				print "    <jar href=\"", $htmldir, "\" main=\"true\" download=\"eager\"/>\n";
			}
 			elsif (/jar$/) {
    				print "    <jar href=\"", $htmldir, "\" main=\"false\" download=\"eager\"/>\n";
			}
		}
    		next
  	}
}




##########################################
#         Generating JNLP file           #
##########################################

print "<?xml version = '1.0' encoding = 'utf-8'?>
  <jnlp spec=\"1.0+\" codebase=\"$codebase/$appdir\">
  <information>
    <title>TIM Viewer ($viewerVersion)</title>
    <vendor>Technical Infrastructure Monitoring (TIM) Team</vendor>
    <homepage href=\"http://timweb\"/>
    <description>The combined GTPM and synoptic viewer</description>
    <icon kind=\"splash\" href=\"http://timweb.cern.ch/img/tim-animated-320x200.gif\"/>
  </information>
  <security> 
    <all-permissions/> 
  </security> 
  <resources>
    <j2se version=\"1.7+\"  initial-heap-size=\"512M\" max-heap-size=\"512M\"/>" , "\n";

jarlist ("$jardir");

# Defines the application name for RBAC
print "    <property name=\"app.name\" value=\"tim-viewer-pro\"/>\n";
# Defines the version number for RBAC
print "    <property name=\"app.version\" value=\"$viewerVersion\"/>\n";
# Defines the version number that is shown in the TIM Viewer about dialog
print "    <property name=\"tim.version\" value=\"$viewerVersion\"/>\n";
# JMS configuration parameters needed by C2MON client API
print "    <property name=\"c2mon.client.conf.url\" value=\"$c2monClientPropertiesURL\"/>\n";
# log4j configuration URL
print "    <property name=\"log4j.configuration\" value=\"$log4jURL\"/>\n";
# Defines the timezone, which is not always correctly picked up from Linux
print "    <property name=\"user.timezone\" value=\"Europe/Zurich\"/>\n";

if (param('configurl')) {
	print "    <property name=\"tim.conf.url\" value=\"", param('configurl'), "\"/>", "\n";
}

print "  </resources>
  <resources os=\"Windows\" >
    <property name=\"tim.log.file\" value=\"c:\\temp\\tim2-viewer.log\"/>
    <property name=\"oracle.net.tns_admin\" value=\"G:\\Applications\\Oracle\\ADMIN\"/>
  </resources>
  <resources os=\"Linux\" >
    <property name=\"tim.log.file\" value=\"/tmp/tim2-viewer.log\"/>
    <property name=\"oracle.net.tns_admin\" value=\"/etc\"/>
  </resources>
  <application-desc main-class=\"ch.cern.tim.client.jviews.Main\"/>
</jnlp>" , "\n";
