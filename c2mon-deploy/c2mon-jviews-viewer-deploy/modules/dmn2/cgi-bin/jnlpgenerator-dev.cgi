#!/usr/bin/perl -wT

use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Cwd;


my $appName = "dmn2-viewer-dev";

#find out the name of the home folder of the application
#home folder is: cgi-bin/../

my $cdir = getcwd;
my @pathtokens = split(/\//,$cdir);

# we are in cgi-bin folder, so the home folder is one level up
my $appdir = @pathtokens[scalar(@pathtokens)-2];


##
# Definition of global variables
##
my $jardir = "../lib";
my $codebase = "http://bewww/~dmndev";
my $c2monClientPropertiesFile = "../conf/client.properties";
my $c2monClientPropertiesURL = "${codebase}/${appdir}/conf/client.properties";

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
##
sub jarlist {
	my $dir = shift;
	opendir DIR, $dir or return;
	my @contents = 
    		map "$dir/$_", 
    		sort grep !/^\.\.?$/,
    		readdir DIR;
  	closedir DIR;
  	foreach (@contents) {
              my $htmldir = $appdir."/".substr($_, 3, length($_));
    		if (!-l && -d) {
			&jarlist($_);
		}
		else {
 			if (/tim-jviews-viewer/) {
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
		<title>DMN2 Viewer [DEV] ($viewerVersion)</title>
	    <vendor>BE/CO-IN DIAMON2 Team</vendor>
	    <homepage href=\"tim-viewer/index.html\"/>
	    <description>The synoptic viewer</description>
	    <icon kind=\"splash\" href=\"http://timweb.cern.ch/img/tim-animated-320x200.gif\"/>
	    <offline-allowed />
	</information>
	<security> 
		<all-permissions/> 
	</security> 
	<resources>
		<j2se version=\"1.7+\"  initial-heap-size=\"512M\" max-heap-size=\"512M\"/>" , "\n";

jarlist ("$jardir");

# set app.name and app.version 
if (param('appName')) {
  print  "		<property name=\"app.name\" value=\"", param('appName'),"\"/>\n";
}
else {
  print  "		<property name=\"app.name\" value=\"$appName\"/>\n";
}

# Defines the version number that is shown in the DMN2 Viewer about dialog
print "		<property name=\"tim.version\" value=\"$viewerVersion\"/>\n";
# JMS configuration parameters needed by C2MON client API
print "		<property name=\"c2mon.client.conf.url\" value=\"$c2monClientPropertiesURL\"/>\n";

if (param('configurl')) {
	print "		<property name=\"tim.conf.url\" value=\"", param('configurl'), "\"/>", "\n";
}

print "         </resources>
        <resources os=\"Windows\" >
                <property name=\"tim.log.file\" value=\"c:\\temp\\tim2-viewer.log\"/>
                <property name=\"oracle.net.tns_admin\" value=\"G:\\Applications\\Oracle\\ADMIN\"/>
        </resources>
        <resources os=\"Linux\" >
                <property name=\"tim.log.file\" value=\"/tmp/tim2-viewer.log\"/>
                <property name=\"oracle.net.tns_admin\" value=\"/etc\"/>
        </resources>
        <application-desc main-class=\"ch.cern.tim.client.jviews.Main\">
        </application-desc>
</jnlp>" , "\n";