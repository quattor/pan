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

my %tplnames;
my %data;
my %deltas;

# Initialize the sequence counter.
my $seqno = 0;

# Open the log file.  
open LOG, '<', $file;
die "problem opening $file: $!\n" if ($?);

# Loop over all of the records, stripping out only those related 
# to the build stages (tag = 'START_*' or 'END_*', excepting COMPILE
# and BUILD). 
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
        
    # Branch on the type of record.
    if ($tag =~ m/START_(.*)/) {

        # Treat a starting tag.

        # Extract the type of this record.
        my $type = $1;

        # If the type is EXECUTE, then ensure that this is listed with
        # the object templates that are treated.
        $tplnames{$tpl} = 1 if ($type eq 'EXECUTE');

        # Save the starting time of this task.
        if ($type ne 'COMPILE' && $type ne 'BUILD') {
            my $index = $type . ':' . $thread . ':' . $tpl;
            $data{$index} = $time;
        }

    } elsif ($tag =~ m/END_(.*)/) {

        # Treat an ending tag.

        # Extract the type of this record.
        my $type = $1;

        # Extract the starting time from the hash.
        if ($type ne 'COMPILE' && $type ne 'BUILD') {
            my $index = $type . ':' . $thread . ':' . $tpl;
            my $t0 = $data{$index};
            
            if (defined($t0)) {
                my $delta = $time - $t0;
                delete($data{$index});
                
                my $key = $type . ':' . $tpl;
                $deltas{$key} = $delta;
                
            } else {
                print STDERR "PROBLEM: no match for $index $t\n";
            }
        }

    }

}

close LOG;
die "error closing $file: $!\n" if ($?);

# Write the table header.
print "execute defaults valid1 valid2 xml dep tpl\n";

# Now loop over the gathered keys, collect information, and print table.
my $seqno = 0;
foreach (sort keys(%tplnames)) {
    $seqno++;

    my $execute = $deltas{"EXECUTE:$_"};
    my $defaults = $deltas{"DEFAULTS:$_"};
    my $valid1 = $deltas{"VALID1:$_"};
    my $valid2 = $deltas{"VALID2:$_"};
    my $xml = $deltas{"XMLFILE:$_"};
    my $dep = $deltas{"DEPFILE:$_"};

    print join(' ', $seqno, $execute, $defaults, $valid1, $valid2, $xml, $dep, $_) . "\n";
}

exit(0);

sub usage() {

    print STDERR <<"EOF"

Extract information about the build statistics from the panc log file.
This prints a table of the time for each stage of the build (in ms)
for each object.  The "task" logging option must have been used when 
running panc.

$0 [--help] {logfile}

EOF
;
    exit(1);
}
