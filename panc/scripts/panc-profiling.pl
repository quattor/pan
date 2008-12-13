#!/usr/bin/perl

use Getopt::Long;

# Initialize the option.
my $help = 0;
my $usefunctions = 0;

# Retrieve the options.
GetOptions('usefunctions' => \$usefunctions, 'help' => \$help);

# Process the help if necessary.
usage() if ($help);

# Set the flag for excluded types.
my $excluded = ($usefunctions) ? '' : 'FUNCTION';

# The argument must be the panc logfile.
my $file = shift || usage();

# Open the log file.  
open LOG, '<', $file;
die "problem opening $file: $!\n" if ($?);

# Create variables to hold the thread to profile mapping.
my %root;
my %paths;

# Loop over all of the records, stripping out only those related 
# to the call tree (tag = 'ENTER' or 'EXIT'). 
while (<LOG>) {

    # The log entries can be parsed by splitting on whitespace.
    chomp($_);
    my ($t, $thread, $tag, $tpltype, $arg1, $arg2) = split('\s+', $_);

    # Usually just the template name is given.  Function calls have more
    # information, so combine the function name with the real template name.
    my $tpl = $arg1;
    if ($tpltype eq 'FUNCTION') {
        $tpl = "$arg2#$arg1";
    }

    if ($tag eq "ENTER" && $tpltype ne $excluded) {

        # Create a new node for this template.
        my $node = create_node($tpl, $t);

        # Create the path for this thread if it doesn't yet exist.
        $paths{$thread} = [] unless defined($paths{$thread});

        # Extract the reference to the current path.
        my $pathref = $paths{$thread};

        if (scalar(@$pathref) == 0) {

            # The path is empty, so add this to the hash of root nodes.
            $root{"$thread:$tpl"} = $node;

        } else {

            # Add this node to the parent.
            add_child($pathref->[scalar(@$pathref)-1], $node);

        }

        # Always push the current node onto the path.
        push @$pathref, $node;

    } elsif ($tag eq 'EXIT' && $tpltype ne $excluded) {

        # Treat an ending tag for an ordinary or structure template.

        # Extract the array for this thread and pop the last node off 
        # of the path.
        my $pathref = $paths{$thread};
        my $popped = pop @$pathref;
        set_ending_time($popped, $t);

        # Check that the popped value is the same as the exit value.
        if ($popped->{'NAME'} ne $tpl) {
            print STDERR "MISMATCH: $tpl != " . $popped->{'NAME'} . "\n";
        }

    }

}

close LOG;
die "error closing $file: $!\n" if ($?);

# Loop over all of the defined root values, printing the top-down 
# profiling analysis.  This is in the form of a tree with the total 
# time at and below each node given.
foreach (keys(%root)) {

    my $node = $root{$_};

    # Create the file name from the hash index.
    $_ =~ m/.*:(.*)/;
    my $file = "$1.topdown.txt";

    # Open the output file. 
    open FILE, '>', "$file";
    print STDERR "problem writing $file: $!\n" if ($?);

    # Create the file contents.
    print_node(FILE, $node, '', '');

    # Close the file. 
    close FILE;
    print STDERR "problem closing $file: $!\n" if ($?);
}

# Loop over all of the defined root values and print the bottom-up
# profiling analysis.  This gives a list of templates (or functions within
# templates) ordered by the amount of time spent in each. 
foreach (keys(%root)) {
    my $node = $root{$_};

    my %info;
    process_node(\%info, $node);

    # Create the file name from the hash index.
    $_ =~ m/.*:(.*)/;
    my $file = "$1.bottomup.txt";

    # Open the output file. 
    open FILE, '>', "$file";
    print STDERR "problem writing $file: $!\n" if ($?);

    # Create the file contents.
    @sorted = reverse sort { $info{$a} <=> $info{$b} } keys %info;
    foreach (@sorted) {
        my $line = sprintf "%8d ms | %s\n", $info{$_}, $_;
        print FILE $line;
    }

    # Close the file. 
    close FILE;
    print STDERR "problem closing $file: $!\n" if ($?);
}

exit(0);

# Creates a new node for the inclusion graph.  The arguments are the 
# name of the node (template name) and the start time.
sub create_node($$) {
    my ($name, $start) = @_;
    my $href = {}; 

    $href->{'NAME'} = $name;
    $href->{'START'} = $start;
    $href->{'INCLUDES'} = [];

    return $href;
}

# Set the ending time for a particular node.  The arguments are the node
# reference and the ending time.
sub set_ending_time($$) {
    my ($node, $end) = @_;
    $node->{'END'} = $end;
}

# Add a child to a given node.  The arguments are the parent's node
# reference and the child's node reference. 
sub add_child($$) {
    my ($parent, $child) = @_;

    my $aref = $parent->{'INCLUDES'};
    push @$aref, $child;
}

# Print a node into the top-down profiling analysis.  The arguments are the
# file descriptor, node reference, and two parameters to print the tree.
# The two last parameters should be the empty string for the first call.
sub print_node($$$$) {
    my ($fileref, $nref, $prefix, $indent) = @_;

    # Extract the node name and array of children.
    my $name = $nref->{'NAME'};
    my $aref = $nref->{'INCLUDES'};
    my $delta = extract_duration($nref);

    print $fileref "$prefix$name ($delta ms)\n";

    # Now recursively treat each child.
    my $limit = scalar(@$aref)-1;
    for (my $i=0; $i<=$limit; $i++) {
        my $cref = $aref->[$i];
        my $term = ($i != $limit) ? '|  ' : '   ';
        print_node($fileref, $cref, $indent . '|--', $indent . $term);
    }

}

# Determine how much time is spent in this node, excluding any time spent 
# in child nodes.  This will recursively process each child as well.  The
# totals are accumulated in the hash given as the first argument.  The
# node reference is the second argument.
sub process_node($$) {
    my ($href, $nref) = @_;

    # Extract the node name and array of children.
    my $name = $nref->{'NAME'};
    my $aref = $nref->{'INCLUDES'};

    # Total duration for this node. 
    my $delta = extract_duration($nref);

    # Find out the total time spent in all of the children.  Process
    # each child as we go along. 
    my $sum = 0;
    foreach (@$aref) {
        $sum += extract_duration($_);
        process_node($href, $_);
    }

    # Update the total time for this template. 
    my $value = (defined($href->{$name})) ? $href->{$name} : 0;
    $value += ($delta-$sum);
    $href->{$name} = $value;
}

# Given a node, extract the starting and ending times, then calculate 
# the duration. 
sub extract_duration($) {
    my ($nref) = @_;

    # Get the starting and ending time.
    my $start = $nref->{'START'};
    my $end = $nref->{'END'};

    # Return the duration of this node.
    return $end-$start;
}

sub usage() {

    print STDERR <<"EOF"

Generates profiling information from the panc log file.  Two files are 
created for each object template: one with 'top-down' profile information 
and the other with 'bottom-up' information.  The "call" logging option 
must have been used when running panc.

$0 [--help] [--usefunctions] {logfile}

EOF
;
    exit(1);
}
