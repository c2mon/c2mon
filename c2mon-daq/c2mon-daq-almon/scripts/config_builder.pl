#!/usr/bin/perl
###
 ##
use strict;             # force variables to be declared
use warnings;           # same as -w on command line

use DBI;                # database access to Oracle if needed

#
# Explicitement exlcus (on elimine aussi par query tous les GD_, cad les global devices)
#
my $blacklist = "'LTL.KVT10'";
#my $whitelist = "'BTY.BVT101ISO'";
my $whitelist = undef;

my $db="dbabco";
my $username="cmw";

my $ACCELERATOR=$ARGV[0];

if (!defined($ACCELERATOR))
{
	die "Missing parameter accelerator";
}

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
open(DA, ">alarm_monitor.xml");

open(HEADER, '<config_header.xml') or die "Could not open file config_header.xml $!";
while (my $row = <HEADER>) 
{
	printf DA "%s", $row;
}

my $tagId = 0;
# =======================================================================================

my $SEL = <<_END_STMT1;
	select class_name, device_name
	from cmw_almon_gm_devices 
	where 
		device_name not in ( $blacklist ) and
		device_name not like 'GD_%' and
		accelerator ='$ACCELERATOR' 
	order by 1
_END_STMT1

my $sth = $dbh->prepare($SEL);
$sth->execute();
while ( my $row = $sth->fetchrow_hashref() )
{
	my $cls = $row->{CLASS_NAME};
	my $dev = $row->{DEVICE_NAME};

#	print_tag ($ff, $fm, $fc, $dtype, $aprop, $fieldname);
	if (!defined $whitelist)
	{
		print_tag  ($cls, $dev, 1, 'GM', 'ALARM', 'value');
	}
}


$SEL= <<_END_STMT2;
	select device_name, field_name, fault_code, property_name, fault_family 
	from cmw_almon_fesa_devices 
	where 
		device_name not in ( $blacklist ) and
		device_name not like 'GD_%' and
		accelerator = '$ACCELERATOR' 
	order by 1,2,3
_END_STMT2
		
$sth = $dbh->prepare($SEL);
$sth->execute();
while ( my $row = $sth->fetchrow_hashref() )
{
        my $cls = $row->{FAULT_FAMILY};
        my $dev = $row->{DEVICE_NAME};
        my $fc = $row->{FAULT_CODE};
        my $fld = $row->{FIELD_NAME};
	my $aprop = $row->{PROPERTY_NAME};

#	print_tag ($ff, $fm, $fc, $dtype, $aprop, $fieldname);
	print_tag  ($cls, $dev, $fc, 'FESA', $aprop, $fld);
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
	my ($ff, $dev, $fc, $dtype, $aprop, $fld) = @_;

	my $fm = uc($dev);
	printf DA "\t<DataTag id=\"%d\" name=\"%s:%s:%d\" control=\"false\">\n", $tagId, $ff, $fm, $fc;
	printf DA "\t\t<data-type>Boolean</data-type>\n";
	printf DA "\t\t<DataTagAddress>\n";
	printf DA "\t\t\t<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl\">\n";
	printf DA "\t\t\t\t<address>\n";
	printf DA "\t\t\t\t{\n";
	printf DA "\t\t\t\t\"type\": \"%s\",\n", $dtype;	
	printf DA "\t\t\t\t\"property\": \"%s\",\n", $aprop;
	printf DA "\t\t\t\t\"device\": \"%s\",\n", $dev;
	printf DA "\t\t\t\t\"field\": \"%s\",\n", $fld;
	printf DA "\t\t\t\t\"alarmTriplet\": {\n";
	printf DA "\t\t\t\t\t\"faultFamily\": \"%s\",\n",$ff;
	printf DA "\t\t\t\t\t\"faultMember\": \"%s\",\n", $fm;
	printf DA "\t\t\t\t\t\"faultCode\": \"%d\"\n", $fc;
	printf DA "\t\t\t\t\t}\n";
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


