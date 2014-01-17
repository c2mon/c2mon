#!/usr/bin/perl
use lib '/user/alaser/perllib/Config-Properties-1.71/blib/lib';
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Config::Properties;
use DBD::Oracle;
use File::Path;
use Cwd;

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

##
# Definition of global variables
##

#find out the name of the home folder of the application
#home folder is: cgi-bin/../

my $cdir = getcwd;
my @pathtokens = split(/\//,$cdir);

# we are in cgi-bin folder, so the home folder is one level up
my $appdir = @pathtokens[scalar(@pathtokens)-2];

my $basedir = "/user/${username_exec}/public_html/dmn2-views";
my $codebase = "http://bewww/~${username_exec}";
my $baseurl = "${codebase}/dmn2-views";

my $c2monClientPropertiesFile= "/user/${username_exec}/public_html/${appdir}/conf/client.properties";

# Reading property file client.properties
open PROPS, "< $c2monClientPropertiesFile"
  or die "Unable to open configuration file $c2monClientPropertiesFile";

my $c2monProperties = new Config::Properties();

$c2monProperties->load(*PROPS);

# Separate database connection URL is neccesary for the perl DBI connector

my $dbiUser = $c2monProperties->getProperty("c2mon.jdbc.config.user");
my $dbiPassword = $c2monProperties->getProperty("c2mon.jdbc.config.password");

# get the url (in java jdbc format)
my $dbiUrl = $c2monProperties->getProperty("c2mon.jdbc.config.url");
# change it to perl dbi format
$dbiUrl =~ s/jdbc:oracle:thin:@/dbi:Oracle:/g;


my $dbh = DBI->connect( $dbiUrl, $dbiUser, $dbiPassword )
  || die( $DBI::errstr . "\n" );

### Start of configuration file generation
#
#  Creating the following structure: Equipment_type/Process/Process-*.tid
#


print "Content-type: text/xml", "\n\n";


### Start of original directory tree parser


print "<FolderList name=\"", filename($basedir), "\">\n";
smash("$basedir");
print "</FolderList>\n";

sub smash {
	my $dir = shift;
	opendir DIR, $dir or return;
	my @contents =
	     map "$dir/$_", sort grep !/^\.\.?$/
	  && !/CVS/
	  && !/lib_.*/
	  && !/libs_.*/
	  && !/.*\.jar/
	  && !/.*\.css/
	  && !/.*\.svg/
	  && !/.*\.gif/
	  && !/.*\.GIF/
	  && !/.*\.jpg/
	  && !/.*\.JPG/,
	  readdir DIR;
	closedir DIR;
	foreach (@contents) {
               
                # skip folders with name starting with 'template'
                if ( filename($_) =~ m/template/ ) {
                        next;
                }

		if ( !-l && -d ) {

			print "<Folder name=\"", filename($_), "\" display=\"",
			  displayname($_), "\">\n";
			&smash($_);
			print "</Folder>\n";
		}
		else {
			print "<Item type=\"", type($_), "\" name=\"", filename($_),
			  "\" display=\"", displayname($_), "\" url=\"", urlname($_),
			  "\">\n";
			opendir DIR2, $dir;
			my @css =
			  map "$dir/$_",
			  sort grep !/^\.\.?$/ && /.*\.css/,
			  readdir DIR2;
			foreach (@css) {
				print "<CSS name=\"", filename($_), "\" display=\"",
				  displayname($_), "\" url=\"", urlname($_), "\"/>\n";
			}
			closedir DIR2;
			print "</Item>\n";
		}
		next

	}
}

sub dirname {
	my $fullname = shift;
	@parts = split( /\//, $fullname );
	pop(@parts);
	$parts;
}

sub type {
	my $fullname = shift;
	my $type     = "UNKNOWN";
	if ( $fullname =~ m/\.ivl/ ) {
		$type = "BGO";
	}
	elsif ( $fullname =~ m/\.xml/ || $fullname =~ m/\.gtpm/ ) {
		$type = "SDM";
	}
	elsif ( $fullname =~ m/\.tid/ ) {
		$type = "TID";
	}
	elsif ( $fullname =~ m/\.idbd/ || $fullname =~ m/\.idbin/ ) {
		$type = "DASHBOARD";
	}
	elsif ( $fullname =~ m/\.trend/ ) {
		$type = "TREND";
	}
	elsif ( $fullname =~ m/\.super/ ) {
		$type = "SUPER";
	}

	$type;
}

sub filename {
	my $fullname = shift;
	@parts = split( /\//, $fullname );
	pop(@parts);
}

sub displayname {
	my $fullname = shift;
	my $shortname;
	@parts     = split( /\//, $fullname );
	$shortname = pop(@parts);
	@parts     = split( /\./, $shortname );
	$shortname = $parts[0];
	$shortname =~ s/_/ /g;
	$shortname =~ s/\b(\w)/\U$1/g;
	$shortname;
}

sub urlname {
	$_ = shift;
	s/$basedir/$baseurl/;
	$_;
}

### End of original tree parser
