#!/usr/bin/perl
use lib '/user/alaser/perllib/Config-Properties-1.71/blib/lib';
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Config::Properties;
use DBD::Oracle;
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

my $basedir = "/user/diamonop/public_html/dmn2-views";
my $codebase = "http://bewww/~diamonop";
my $baseurl = "${codebase}/dmn2-views";

my $c2monClientPropertiesFile= "/user/diamonop/public_html/${appdir}/conf/client.properties";

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



## Fetch current equipment types

rename( "${basedir}/DAQ", "/dev/null/1" );    # Drop previous structure
mkdir("${basedir}/DAQ");

my $fetch_types_sql = <<END;
select distinct equipment_type_name from DMN_PROCESSES_V
END

my $sth = $dbh->prepare($fetch_types_sql)
  || die "Couldn't prepare statement: " . $dbh->errstr;
my @data;
$sth->execute()
  || die "Couldn't execute statement: " . $sth->errstr;

while ( @data = $sth->fetchrow_array() ) {
	my $equipment_type = $data[0];

	mkdir("${basedir}/DAQ/${equipment_type}");

	fetchProcesses($equipment_type);

}
if ( $sth->rows == 0 ) {
	# print "No equipment types are defined in the current configuration.\n\n";
}

	my $entity_type = "COMPUTER";
	rename( "${basedir}/${entity_type}", "/dev/null/1" )
	  ;    # Drop previous structure
	mkdir("${basedir}/${entity_type}");

	fetchEntities($entity_type);


### End of configuration file generation

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

### Additional subs used for tid file generation by Peter Jurcso

### Fetch current processes

sub fetchProcesses {

	my $equipment_type = $_[0];

	my $fetch_processes_sql = <<END;
select process_name,process_id,process_alive_tag_id,PROCESS_STATE_TAG_ID from DMN_PROCESSES_V where  EQUIPMENT_TYPE_NAME=
END

	my $sth = $dbh->prepare("${fetch_processes_sql} '${equipment_type}'")
	  || die "Couldn't prepare statement: " . $dbh->errstr;
	my @data;
	$sth->execute()
	  || die "Couldn't execute statement: " . $sth->errstr;

	while ( @data = $sth->fetchrow_array() ) {
		my $process_name         = $data[0];
		my $process_id           = $data[1];
		my $process_alive_tag_id = $data[2];
		my $process_state_tag_id = $data[3];

		mkdir("${basedir}/DAQ/${equipment_type}/${process_name}");
		open( MYFILE,
" > ${basedir}/DAQ/${equipment_type}/${process_name}/${process_name}-process.tid"
		);
		print MYFILE "${process_alive_tag_id}\n";
		print MYFILE "${process_state_tag_id}\n";
		close(MYFILE);

		##fetchEquipments( $process_id, $process_name, $equipment_type );
		fetchEquipmentAlives( $process_id, $process_name, $equipment_type );
		fetchTags( $process_id, $process_name, $equipment_type );
		fetchLimits( $process_id, $process_name, $equipment_type );

	}
	if ( $sth->rows == 0 ) {
		print
"No process are defined for the selected equipment types in the current configuration.\n\n";
	}
	$sth->finish;

}

## Fetch status of equipments attached to the active processes

sub fetchEquipmentAlives {

	my $process_id     = $_[0];
	my $process_name   = $_[1];
	my $equipment_type = $_[2];

	my $fetch_equipments_sql = <<END;
select EQUIPMENT_ALIVE_TAG_ID from DMN_EQUIPMENT_V where process_id=
END

	my $sth = $dbh->prepare("${fetch_equipments_sql} ${process_id}")
	  || die "Couldn't prepare statement: " . $dbh->errstr;
	my @data;
	$sth->execute()
	  || die "Couldn't execute statement: " . $sth->errstr;

	open( MYFILE,
" > ${basedir}/DAQ/${equipment_type}/${process_name}/${process_name}-equipment-alives.tid"
	);

	while ( @data = $sth->fetchrow_array() ) {
		my $tag_id = $data[0];

		print MYFILE "${tag_id}\n";

	}
	if ( $sth->rows == 0 ) {
#		print
# "No equipments are defined for the selected equipment types in the current configuration.\n\n";
	}
	$sth->finish;
	close(MYFILE);

}

## Fetch metric data tags of equipments attached to the active processes

sub fetchTags {

	my $process_id     = $_[0];
	my $process_name   = $_[1];
	my $equipment_type = $_[2];

	my $fetch_tags_sql = <<END;
select metric_data_tag_id from DMN_METRICS_V m, DMN_EQUIPMENT_V e where e.equipment_id=m.equipment_id and m.metric_data_tag_id is not null and m.metric_control_flag='N' and e.process_id=
END

	my $sth = $dbh->prepare("${fetch_tags_sql} ${process_id}")
	  || die "Couldn't prepare statement: " . $dbh->errstr;
	my @data;
	$sth->execute()
	  || die "Couldn't execute statement: " . $sth->errstr;

	open( MYFILE,
" > ${basedir}/DAQ/${equipment_type}/${process_name}/${process_name}-metrics.tid"
	);
	while ( @data = $sth->fetchrow_array() ) {
		my $tag_id = $data[0];

		print MYFILE "${tag_id}\n";

	}
	if ( $sth->rows == 0 ) {
#		print
#"No data tags are defined for the selected equipment types in the current configuration.\n\n";
	}
	$sth->finish;
	close(MYFILE);

}

## Fetch rule tags of equipments attached to the active processes

sub fetchLimits {

	my $process_id     = $_[0];
	my $process_name   = $_[1];
	my $equipment_type = $_[2];

	my $fetch_tags_sql = <<END;
select metric_rule_tag_id from DMN_METRICS_V m, DMN_EQUIPMENT_V e where e.equipment_id=m.equipment_id and m.metric_rule_tag_id is not null and limit_flag='Y' and e.process_id=
END

	my $sth = $dbh->prepare("${fetch_tags_sql} ${process_id}")
	  || die "Couldn't prepare statement: " . $dbh->errstr;
	my @data;
	$sth->execute()
	  || die "Couldn't execute statement: " . $sth->errstr;

	open( MYFILE,
" > ${basedir}/DAQ/${equipment_type}/${process_name}/${process_name}-limits.tid"
	);
	while ( @data = $sth->fetchrow_array() ) {
		my $tag_id = $data[0];

		print MYFILE "${tag_id}\n";

	}
	if ( $sth->rows == 0 ) {
#		print
#"No limits are defined for the selected equipment types in the current configuration.\n\n";
	}
	$sth->finish;
	close(MYFILE);

}

sub fetchEntities {

	my $entity_type = $_[0];

	my $fetch_entities_sql = <<END;
select COMPUTER_RULE_TAG_ID from DMN_COMPUTERS_V where COMPUTER_RULE_FLAG='Y'
END

	my $sth = $dbh->prepare("${fetch_entities_sql}")
	  || die "Couldn't prepare statement: " . $dbh->errstr;
	my @data;
	$sth->execute()
	  || die "Couldn't execute statement: " . $sth->errstr;
	open( MYFILE, " > ${basedir}/${entity_type}/${entity_type}-rules.tid" );
	while ( @data = $sth->fetchrow_array() ) {
		my $tag_id = $data[0];
		print MYFILE "${tag_id}\n";
	}

	close(MYFILE);

	if ( $sth->rows == 0 ) {
#		print
#"No process are defined for the selected equipment types in the current configuration.\n\n";
	}
	$sth->finish;

}
