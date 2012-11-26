#!/usr/bin/perl
use lib '/user/alaser/perllib/Config-Properties-1.71/blib/lib';
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser); use Config::Properties; use DBD::Oracle; use File::Path; use Cwd 'abs_path';
use IO::Socket::INET;

##
# Definition of global variables
##

#find out the name of the home folder of the application 
#home folder is: bin/../

my $cdir =  abs_path($0);

my @pathtokens = split(/\//,$cdir);

# we are in bin folder, so the home folder is one level up 
my $appdir = @pathtokens[scalar(@pathtokens)-3];

my $tidfile = "/opt/${appdir}/conf/publisher-new.tid";

my $c2monClientPropertiesFile= "/opt/${appdir}/conf/client.properties";

# Reading property file client.properties 
open PROPS, "< $c2monClientPropertiesFile"
  or die "Unable to open configuration file $c2monClientPropertiesFile";

my $c2monProperties = new Config::Properties();

$c2monProperties->load(*PROPS);


##
# Network UDP connection
##

$socket = new IO::Socket::INET (
PeerHost => 'cs-ccr-inf1.cern.ch',
PeerPort => '12409',
Proto => 'udp',
) or die "ERROR in Socket Creation : $!\n";

# Separate database connection URL is necessary for the perl DBI connector

my $dbiUser = $c2monProperties->getProperty("c2mon.jdbc.config.user");
my $dbiPassword = $c2monProperties->getProperty("c2mon.jdbc.config.password");

# get the url (in java jdbc format)
my $dbiUrl = $c2monProperties->getProperty("c2mon.jdbc.config.url");
# change it to perl dbi format
$dbiUrl =~ s/jdbc:oracle:thin:@/dbi:Oracle:/g;

my $dbh = DBI->connect( $dbiUrl, $dbiUser, $dbiPassword )
  || die( $DBI::errstr . "\n" );


my $fetch_configuration_sql = <<END;
SELECT compname,substr(nvl(replace(compdescrip,'#',''),'NA'),1,33) compdescrip,operating_system,
LISTAGG(metric_data_tag_id, ',')  WITHIN GROUP (ORDER BY metric_data_tag_id) AS metrics,
count(metric_data_tag_id)
FROM   DMN_METRICS_V,DMN_COMPUTERS    where enabled_flag='Y'     and compname=lower(equipment_short_name)
and comptype='DSC'
and diamon_flag='Y'
and rule_flag='Y'
and  operating_system is not null
and metric_short_name in ('PROC.ACTIVESTATE.ABSOLUTE','PROC.ACTIVESTATE.DELTA','PROC.ACTIVEUSERS',
'SYS.KERN.IDLE','SYS.KERN.IRQ','SYS.KERN.NICE','SYS.KERN.SIRQ','SYS.KERN.SYSTEM','SYS.KERN.UPTIME',
'SYS.KERN.USER','SYS.LOADAVG','SYS.MEM.BUFFERED','SYS.MEM.CACHED','SYS.MEM.FREE','SYS.MEM.INACTPCT',
'SYS.MEM.SWAPPCT','SYS.MEM.USED','SYS.NET.IN','SYS.NET.OUT')
GROUP BY compname,compdescrip,operating_system     order by compname
END

my $sth = $dbh->prepare($fetch_configuration_sql)
  || die ":Couldn't prepare statement: " . $dbh->errstr;
my @data;
$sth->execute()
  || die ":Couldn't execute statement: " . $sth->errstr;



while ( @data = $sth->fetchrow_array() ) 
{
                
                $hostName=$data[0];
                $hostDesc=$data[1];
                $hostOS=$data[2];
                $hostMetrics=$data[3];
                $hostMetricsCount=$data[4];
                $totalMetricsCount=$totalMetricsCount+$hostMetricsCount;
                
                $hostDesc =~ s/ /_/g;
                
                my @host = gethostbyname("$hostName.cern.ch");
    if (scalar(@host) == 0) {
        $addr = "0.0.0.0";
        # print "$hostName is not in DNS!\n";
    } else {
        $addr = inet_ntoa($host[4]);
        $ep=time;
                                $data="A1 0 $hostName#9200 $ep eth0 $addr 255.255.0.0 172.18.200 172.18.1.1 00:00:00:00:00:00 1500 1 1024000#$hostName#4109 $ep $hostOS $hostOS $hostDesc $hostOS#";

                                # Inject data into Lemon
                                print $socket "$data\n"
                                || die "ERROR in Socket usage error : $!\n";

                                $finalMetricsList=   "$finalMetricsList$hostMetrics,";
                                $agents++;
    }
                
}

if ( $sth->rows == 0 ) { print("No configuration.\n"); }

$sth->finish;

$dbh->disconnect;

##
#  End of database conncetions
##

$socket->close();

##
#  End of UDP conncetion
##
       
        
open( result, " > $tidfile" )
|| print "ERROR:Unable to create file $tidfile\n";

$finalMetricsList=~ s/,/\n/g;

print result $finalMetricsList
|| print "ERROR:Unable to write into file $tidfile\n";

close(result)
|| print "ERROR:Unable to close file $tidfile\n";

#print "$tidfile for LEMON publihser has been prepared. Total $totalMetricsCount\n\n"; 