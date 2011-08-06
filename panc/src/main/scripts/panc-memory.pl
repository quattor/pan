#!/usr/bin/perl

use Getopt::Long;

# Initialize the option.
my $help = 0;

# Retrieve the options.
GetOptions('help' => \$help);

# Process the help if necessary.
usage() if ($help);

# The argument must be the panc logfile.
my $file = shift || usage();

# Set the base time to a negative number.  Collect the real time
# from the first entry.
my $basetime = undef;

# Initialize the sequence counter.
my $seqno = 0;

# Open the log file.  
open LOG, '<', $file;
die "problem opening $file: $!\n" if ($?);

# Write the table header.
print "time memory\n";

# Loop over all of the records, stripping out only those related 
# to memory usage (tag = 'MEM'). 
while (<LOG>) {

    # The log entries can be parsed by splitting on whitespace.
    chomp($_);
    my ($t, $thread, $tag, $memory) = split('\s+', $_);

    # Reset the base time if necessary.
    $basetime = $t unless defined($basetime);

    # Only treat the record if this is a MEM entry.
    if ($tag eq 'MEM') {

        # Increment the sequence number.
        $seqno++;

        # Renormalize the time.
        my $time = $t - $basetime;
        
        # Print out the table entry.  The shift by 20 bits is a 
        # division by 1024*1024 to convert from bytes to megabytes.
        print join(' ', $seqno, $time, ($memory >> 20)) . "\n";
    } 

}

close LOG;
die "error closing $file: $!\n" if ($?);

exit(0);

sub usage() {

    print STDERR <<"EOF"

Extract memory information from panc log file and produce a table 
with the memory utilization (in MB) versus time (in ms).  The 
"memory" logging option must have been used when running panc.

$0 [--help] {logfile}

EOF
;
    exit(1);
}
