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

my %data;

# Initialize the sequence counter.
my $seqno = 0;

# Open the log file.  
open LOG, '<', $file;
die "problem opening $file: $!\n" if ($?);

# Write the table header.
print "start duration tpl\n";

# Loop over all of the records, stripping out only those related 
# to compilation (tag = '*_COMPILE'). 
while (<LOG>) {

    # The log entries can be parsed by splitting on whitespace.
    chomp($_);
    my ($t, $thread, $tag, $tpl) = split('\s+', $_);

    # Reset the base time if necessary.
    $basetime = $t unless defined($basetime);

    # Ensure that the file name is defined.
    $tpl = 'EMPTY' unless defined($tpl);

    # Renormalize the time.
    my $time = $t - $basetime;
        
    # Only treat the record if this is a MEM entry.
    if ($tag eq 'START_COMPILE') {

        # Insert the value into the hash.
        $data{$tpl} = $time;

    } elsif ($tag eq 'END_COMPILE') {

        # Increment the sequence number.
        $seqno++;

        # Calculate the duration.
        my $t0 = $data{$tpl};
        my $duration = $time - $t0;

        # Print out the table entry.
        print join(' ', $seqno, $t0, $duration, $tpl) . "\n";

    }

}

close LOG;
die "error closing $file: $!\n" if ($?);

exit(0);

sub usage() {

    print STDERR <<"EOF"

Extract information about the compilation statistics from the panc
log file.  This produces a table with the start time of the compilation
(in ms), the duration of the compilation (in ms), and the template name.
The "task" logging option must have been used when running panc.

$0 [--help] {logfile}

EOF
;
    exit(1);
}
