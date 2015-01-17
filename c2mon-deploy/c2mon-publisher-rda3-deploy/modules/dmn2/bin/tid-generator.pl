#!/usr/bin/perl
use lib '/user/alaser/perllib/Config-Properties-1.71/blib/lib';
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser); use Config::Properties; use DBD::Oracle; use File::Path; use Cwd 'abs_path';

##
# Definition of global variables
##

#find out the name of the home folder of the application 
#home folder is: bin/../

my $cdir =  abs_path($0);

my @pathtokens = split(/\//,$cdir);

# we are in bin folder, so the home folder is one level up 
my $appdir = @pathtokens[scalar(@pathtokens)-3];

my $tidfile = "/opt/dmn2/${appdir}/conf/publisher-new.tid";

my $c2monClientPropertiesFile= "/opt/dmn2/${appdir}/conf/client.properties";

# Reading property file client.properties 
open PROPS, "< $c2monClientPropertiesFile"
  or die "Unable to open configuration file $c2monClientPropertiesFile";

my $c2monProperties = new Config::Properties();

$c2monProperties->load(*PROPS);

# Separate database connection URL is necessary for the perl DBI connector

my $dbiUser = $c2monProperties->getProperty("c2mon.jdbc.config.user");
my $dbiPassword = $c2monProperties->getProperty("c2mon.jdbc.config.password");

# get the url (in java jdbc format)
my $dbiUrl = $c2monProperties->getProperty("c2mon.jdbc.config.url");
# change it to perl dbi format
$dbiUrl =~ s/jdbc:oracle:thin:@/dbi:Oracle:/g;


my $dbh = DBI->connect( $dbiUrl, $dbiUser, $dbiPassword )
  || die( $DBI::errstr . "\n" );


my $fetch_equipments_sql = <<END;
select metric_data_tag_id from dmn_metrics_v where enabled_flag='Y' and japc_metric_pub_flag='Y'
union
select metric_rule_tag_id as metric_data_tag_id from dmn_metrics_v where enabled_flag='Y' and japc_limit_pub_flag='Y'
END

my $sth = $dbh->prepare("${fetch_equipments_sql}")  
    || die "Couldn't prepare statement: " . $dbh->errstr; my @data;
$sth->execute()
    || die "Couldn't execute statement: " . $sth->errstr;

open( MYFILE,
" > ${tidfile}"
);

while ( @data = $sth->fetchrow_array() ) {
  my $tag_id = $data[0];

  print MYFILE "${tag_id}\n";

}

if ( $sth->rows == 0 ) {
  # print "No tags are defined to be published to RDA.\n\n"; 
}
  
$sth->finish;
close(MYFILE);