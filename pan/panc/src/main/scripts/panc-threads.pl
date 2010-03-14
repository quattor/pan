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

# Open the log file.  
open LOG, '<', $file;
die "problem opening $file: $!\n" if ($?);

# Use two hashes to keep track of the starting and ending times
# for the activity on each thread.
my %start;
my %stop;

# Write the table header.
print "thread start stop\n";

# Loop over all of the records, stripping out only those related 
# to the build process (tags start with 'START_' or 'END_'). 
while (<LOG>) {

    # The log entries can be parsed by splitting on whitespace.
    chomp($_);
    my ($t, $thread, $tag) = split('\s+', $_);

    # Reset the base time if necessary.
    $basetime = $t unless defined($basetime);

    # Only treat the record if this is part of the build process.
    if (($tag =~ m/START_/) || ($tag =~ m/END_/)) {

        # Increment the sequence number.
        $seqno++;

        # Renormalize the time.
        my $time = $t - $basetime;

        # Add the start entry if it hasn't been defined yet.  Always
        # update the stop time assuming that the log file is written
        # in time order.
        $start{$thread} = $time unless defined($start{$thread});
        $stop{$thread} = $time;
    } 

}

close LOG;
die "error closing $file: $!\n" if ($?);

# Now print out the collected information.
my $seqno = 0;
foreach (sort { $a <=> $b } keys(%start)) {
    my $begin = $start{$_};
    my $end = $stop{$_};
    $seqno++;
    print join(' ', $seqno, $_, $begin, $end) . "\n";
}

exit(0);

sub usage() {

    print STDERR <<"EOF"

Extract information about thread activity during the build process.  
Prints a table with the start time and ending time of each thread.  The
"task" logging option must have been used when running panc.

$0 [--help] {logfile}

EOF
;
    exit(1);
}
