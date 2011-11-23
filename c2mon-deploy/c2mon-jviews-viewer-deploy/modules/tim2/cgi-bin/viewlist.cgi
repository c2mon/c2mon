#!/usr/bin/perl
print "Content-type: text/xml" , "\n\n";

my $basedir = "/var/www/html/Views";
my $baseurl = "http://timweb.cern.ch/Views";

print "<FolderList name=\"", filename($basedir), "\">\n";
smash("$basedir");
print "</FolderList>\n";


sub smash {
  my $dir = shift;
  opendir DIR, $dir or return;
  my @contents =
    map "$dir/$_",
    sort grep !/^\.\.?$/&&!/CVS/&&!/lib_.*/&&!/libs_.*/&&!/.*\.jar/&&!/.*\.css/&&!/.*\.svg/&&!/.*\.gif/&&!/.*\.GIF/&&!/.*\.jpg/&&!/.*\.JPG/,
    readdir DIR;
  closedir DIR;
  foreach (@contents) {
    if (!-l && -d) {

    print "<Folder name=\"", filename($_), "\" display=\"", displayname($_), "\">\n";
    &smash($_);
    print "</Folder>\n";
    }
    else {
      print "<Item type=\"", type($_), "\" name=\"", filename($_), "\" display=\"", displayname($_), "\" url=\"", urlname($_), "\">\n";
      opendir DIR2, $dir;
      my @css =
        map "$dir/$_",
        sort grep !/^\.\.?$/&&/.*\.css/,
	readdir DIR2;
      foreach (@css) {
        print "<CSS name=\"", filename($_), "\" display=\"", displayname($_), "\" url=\"", urlname($_), "\"/>\n";
      }
      closedir DIR2;
      print "</Item>\n"
    }
    next

  }
}

sub dirname {
  my $fullname = shift;
  @parts = split(/\//, $fullname);
  pop(@parts);
  $parts
}

sub type {
  my $fullname = shift;
  my $type = "UNKNOWN";
  if ($fullname =~ m/\.ivl/) {
    $type="BGO";
  }
  elsif ($fullname =~ m/\.xml/ || $fullname =~ m/\.gtpm/) {
    $type="SDM";
  }
  elsif ($fullname =~ m/\.tid/) {
    $type="TID";
  }
  elsif ($fullname =~ m/\.idbd/ || $fullname =~ m/\.idbin/) {
    $type="DASHBOARD";
  }
  elsif ($fullname =~ m/\.trend/) {
    $type="TREND";
  }
  elsif ($fullname =~ m/\.super/) {
    $type="SUPER";
  }

  $type;
}

sub filename {
  my $fullname = shift;
  @parts = split(/\//, $fullname);
  pop(@parts)
}


sub displayname {
  my $fullname = shift;
  my $shortname;
  @parts = split(/\//, $fullname);
  $shortname= pop(@parts);
  @parts = split(/\./, $shortname);
  $shortname= $parts[0];
  $shortname=~s/_/ /g;
  $shortname =~ s/\b(\w)/\U$1/g;
  $shortname
}

sub urlname {
  $_ = shift;
  s/$basedir/$baseurl/;
  $_
}
