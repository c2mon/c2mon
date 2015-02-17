#!/usr/bin/perl
###
 ##
use strict;             # force variables to be declared
use warnings;           # same as -w on command line

use DBI;                # database access to Oracle if needed

my $db="laserdb";
my $username="laser";

# password on first line of password file!
open (PASSWD, ".dbpasswd");
my $record = <PASSWD>;
chomp($record);
my $passwd=$record;
close(PASSWD);

my $dbh = DBI->connect( "dbi:Oracle:$db", $username, $passwd ) || die( $DBI::errstr . "\n" );

$dbh->{AutoCommit}    = 0;
$dbh->{RaiseError}    = 1;
$dbh->{ora_check_sql} = 0;
$dbh->{RowCacheSize}  = 16;

print "Extract alarms data ...\n";
# =======================================================================================
open(DA, ">spectrum_monitor.xml");

open(HEADER, '<config_header.xml') or die "Could not open file config_header.xml $!";
while (my $row = <HEADER>) 
{
	printf DA "%s", $row;
}

my $tagId = 0;
# =======================================================================================

my $SEL = <<_END_STMT1;
	select
		ad.fault_member
	from
		alarm_definition ad,
		alarm_category ac
	where
		ad.alarm_id = ac.alarm_id and
		ad.fault_family in ('HOST', 'NETWORK')  and 
		ad.fault_member not like 'CFVM-%' and
		ad.fault_member not like 'SET_%' and
		ad.fault_member not like 'CW%'
	order by 1
_END_STMT1

my $sth = $dbh->prepare($SEL);
$sth->execute();
while ( my $row = $sth->fetchrow_hashref() )
{
	my $fm = $row->{FAULT_MEMBER};
	print_tag  ("HOST", $fm, 1);
}


open(FOOTER, '<config_footer.xml') or die "Could not open file config_footer.xml $!";
while (my $row = <FOOTER>) 
{
	printf DA "%s", $row;
}

exit;


sub print_tag
{
	$tagId++;
	my ($ff, $fm, $fc) = @_;

	my $hostname = lc($fm);
	printf DA "\t<DataTag id=\"%d\" name=\"%s:%s:%d\" control=\"false\">\n", $tagId, $ff, $fm, $fc;
	printf DA "\t\t<data-type>Boolean</data-type>\n";
	printf DA "\t\t<DataTagAddress>\n";
	printf DA "\t\t\t<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl\">\n";
	printf DA "\t\t\t\t<address>\n";
	printf DA "\t\t\t\t{\n";
	printf DA "\t\t\t\t\"hostname\": \"%s\",\n", $hostname;	
	printf DA "\t\t\t\t}\n";
	printf DA "\t\t\t\t</address>\n";
	printf DA "\t\t\t</HardwareAddress>\n";
	printf DA "\t\t\t<time-to-live>3600000</time-to-live>\n";
	printf DA "\t\t\t<priority>2</priority>\n";
	printf DA "\t\t\t<guaranteed-delivery>true</guaranteed-delivery>\n";
	printf DA "\t\t</DataTagAddress>\n";
	printf DA "\t</DataTag>\n";
}


# =======================================================================================

###
 #  ------------------------------ catch all exceptions -----------------------------------
 ##

END
{
        print "Disconnecting from database and closing all file handles ...\n";
    	$dbh->disconnect if defined($dbh);
    	close(DA);
		close(HEADER);
		close(FOOTER);
        print "Completed.\n";
}


