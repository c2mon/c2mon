#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Config::Properties;

##
# Definition of global variables
#
my $jardir = "../lib";
my $appdir = "tim2-dashboard-editor/";
# Default codebase points to operation
my $codebase = "http://timweb.cern.ch/javaws";
my $c2monClientPropertiesURL  = "http://timweb/conf/c2mon-client.properties";

##
# Reading version number from ../version.txt
#
open VFILE, "< ../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

##
# In case of a SNAPSHOT the codebase will point to test
#
if ($viewerVersion =~ /-SNAPSHOT/) {
  $codebase = "http://timweb.cern.ch/test/javaws";
}

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
      		<title>TIM Dashboard Editor</title>
      		<vendor>Technical Infrastructure Monitoring (TIM) Team</vendor>
      		<homepage href=\"http://timweb.cern.ch\"/>
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
# Configuration parameters needed by C2MON client API
print "   <property name=\"c2mon.client.conf.url\" value=\"$c2monClientPropertiesURL\"/>\n";

print " </resources>
        <resources os=\"Windows\" >
                <property name=\"tim.log.file\" value=\"c:\\temp\\tim-dashboard-editor.log\"/>
        </resources>
        <application-desc main-class=\"ch.cern.tim.jviews.TimDashboardEditorMain\">
        </application-desc>
</jnlp>" , "\n";
