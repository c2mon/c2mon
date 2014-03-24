#!/usr/bin/perl
use lib '/user/alaser/perllib/Config-Properties-1.71/blib/lib';
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Config::Properties;
use File::Path;
use Cwd;

##
# Definition of global variables
##

#find out the name of the home folder of the application
#home folder is: cgi-bin/../

my $cdir = getcwd;
my @pathtokens = split(/\//,$cdir);

# we are in cgi-bin folder, so the home folder is one level up
my $appdir = @pathtokens[scalar(@pathtokens)-2];

my $basedir = "/user/dmndev/public_html/dmn2-views";
my $codebase = "http://bewww/~dmndev";
my $baseurl = "${codebase}/dmn2-views";

my $c2monClientPropertiesFile= "/user/dmndev/public_html/${appdir}/conf/client.properties";

# Reading property file client.properties
open PROPS, "< $c2monClientPropertiesFile"
  or die "Unable to open configuration file $c2monClientPropertiesFile";

my $c2monProperties = new Config::Properties();

$c2monProperties->load(*PROPS);


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
