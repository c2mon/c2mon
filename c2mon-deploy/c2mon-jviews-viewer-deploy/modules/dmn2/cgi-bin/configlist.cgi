#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);

##
# Verification if this is the current running instance of the script
##

use Proc::ProcessTable;
my $instcount = 0;
my $insttable = Proc::ProcessTable->new;
for my $instprocess ( @{ $insttable->table } ) 
{
  next unless $instprocess->{cmndline};
  if ($instprocess->{cmndline} =~ /$0/) 
  {
	  $instcount++;
      exit if $instcount > 1;
  }
}

## We have just one instance, let's continue to see if we are running as the correct user

my @valid_users = ("diamonop","dmndev");
my $username_exec=$ENV{"LOGNAME"};

if (!grep {$_ eq $username_exec} @valid_users) 
{
   print "Please start as diamonop or dmndev user\n\n";
   exit;
}

	my $suffix= 'pro';
	my $usuffix = uc $suffix;

if ($username_exec eq "dmndev")
{
	$suffix= 'dev';
	$usuffix = uc $suffix;
}

my $appName = "dmn2-viewer-${suffix}";

my $configsubdir = "/~${username_exec}/dmn2-viewer-dev/conf/${suffix}";
my $configdir = "/user/${username_exec}/public_html/dmn2-viewer-${suffix}/conf/${suffix}";
my $configurl = "http://" . $ENV{'HTTP_HOST'} . $configsubdir;

##
# Reading version number from ../version.txt
#
open VFILE, "<../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

print header;
print start_html(-title=>'DMN2 Viewer [${usuffix}]', -style=>"/css/tim.css");
print h1("DMN2 Viewer [${usuffix}] version: $viewerVersion connects to the DMN2 ${usuffix} server");
print p("To launch the DMN2 Viewer, choose one of the configurations below ",font({-color=>"#FF0000"},"(NOTE: the DMN2 Viewer works correctly only on Technical Network)"),":"); 
opendir DIR, $configdir;
my @files = sort grep !/^\.\.?$/, readdir DIR;
closedir DIR;
foreach (@files) {
  my $fn = $_;
  $fn =~ s/.xml//g;
  print "Start the DMN2 Viewer with configuration ";
  print a({-href=>"jnlpgenerator-dev.cgi?configurl=$configurl/$_"}, $fn);
  print "<br>";
  
  next;
}
print end_html;
