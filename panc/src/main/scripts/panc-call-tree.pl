#!/usr/bin/perl

use Switch;
use Getopt::Long;

# Global variables to store the nodes and edges for
# HyperGraph format
my $hg_nodes="";
my $hg_edges="";

# Initialize the options.
my $help = 0;
my $format = "dot";

# Retrieve the options.
GetOptions('format=s' => \$format, 'help' => \$help);

# Process the help if necessary.
usage() if ($help);

# Check the format.
($format eq "dot" || $format eq "hg") || usage();

# The argument must be the panc logfile.
my $file = shift || usage();


# Open the log file.  
open LOG, '<', $file;
die "problem opening $file: $!\n" if ($?);

# Create variables to hold the thread to profile mapping.
my %root;
my %paths;

# Loop over all of the records, stripping out only those related 
# to the include graph. 
while (<LOG>) {

    # The log entries can be parsed by splitting on whitespace.
    # The format gives the time stamp, thread ID, entry tag, 
    # template type, and template name, for the entries that 
    # interest us.
    chomp($_);
    my ($t, $thread, $tag, $tpltype, $tpl) = split('\s+', $_);

    if ($tag eq 'ENTER' && $tpltype ne 'FUNCTION') {

        # Treat a starting tag for a real template.

        # Select the type of the node, use an ellipse for all 'normal'
        # templates and a box for structure templates.
        my $type="";
        if ($format eq "dot") {
            $type = ($tpltype eq 'STRUCTURE') ? 'box' : 'ellipse';
        } elsif ($format eq "hg") {
            $type = ($tpltype eq 'STRUCTURE') ? 'structure' : 'normal';
        }

        # Create a new node for this template.
        my $node = create_node($tpl, $type);

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

    } elsif ($tag eq 'EXIT' && $tpltype ne 'FUNCTION') {

        # Treat an ending tag for an ordinary or structure template.

        # Extract the array for this thread and pop the last node off 
        # of the path.
        my $pathref = $paths{$thread};
        my $popped = pop @$pathref;

        # Check that the popped value is the same as the exit value.
        if ($popped->{'NAME'} ne $tpl) {
            print STDERR "MISMATCH: $tpl != " . $popped->{'NAME'} . "\n";
        }

    }

}

close LOG;
die "error closing $file: $!\n" if ($?);

# Loop over all of the defined root values, printing a dot or 
# hypergraph file for each one.  The name of the file will be
# the name of the root (object) template with an appropriate
# suffix.
foreach (keys(%root)) {
    my $node = $root{$_};

    # Create the file name from the hash index.
    $_ =~ m/.*:(.*)/;
    my $file="";

    if ($format eq "dot") {
        $file = "$1.dot";
    } elsif ($format eq "hg") {
        $file = "$1.xml";
        # set root node to be of type "root"
        $node->{'TYPE'}="root";
    }   

    # Open the output file. 
    open FILE, '>', "$file";
    print STDERR "problem writing $file: $!\n" if ($?);

    # Create the file contents.

    switch ($format) {
        case "dot" {
            print_dot_header(FILE, $node);
            print_dot_node(FILE, $node);
            print_dot_footer(FILE);
        }
        case "hg" {
            print_hg_header(FILE, $node);
            print_hg_node(FILE, $node);
            print_hg_footer(FILE);
        }
    }
    # Close the file. 
    close FILE;
    print STDERR "problem closing $file: $!\n" if ($?);
}

exit(0);

# Creates a new node for the inclusion graph.  The arguments are the 
# name of the node (template name) and the type.  The type should be
# a node shape known to dot. 
sub create_node($$) {
    my ($name, $type) = @_;
    my $href = {}; 

    $href->{'NAME'} = $name;
    $href->{'TYPE'} = $type;
    $href->{'INCLUDES'} = [];

    return $href;
}

# Add a child to a given node.  The arguments are the parent's node
# reference and the child's node reference. 
sub add_child($$) {
    my ($parent, $child) = @_;

    my $aref = $parent->{'INCLUDES'};
    push @$aref, $child;
}

# Print the header for the dot file.  The arguments are the file descriptor
# and the reference to the root node.  The name of the graph will be the 
# name of the root node (template).
sub print_dot_header($$) {
    my ($fileref, $nref) = @_;
    print $fileref 'digraph "' . $nref->{'NAME'} . "\" {\n";
}

# Print the footer for the dot file.  The argument is just the file
# descriptor.
sub print_dot_footer($) {
    my ($fileref) = @_;
    print $fileref "}\n";
}

# Print a node into the dot file.  The arguments are the file descriptor 
# and the node reference.  Each node will print a shape definition and one
# edge for each child.  This will print nothing if the node has no children.
sub print_dot_node($$) {
    my ($fileref, $nref) = @_;

    # Extract the node name and array of children.
    my $name = $nref->{'NAME'};
    my $aref = $nref->{'INCLUDES'};

    # Only need to do something if there are some children.
    if (scalar(@$aref) > 0) {

        # Print the type of node. 
        my $type = $nref->{'TYPE'};
        print $fileref "node [shape=$type];\n";

        # Print the link to each one of the children.
        foreach (@$aref) {
            my $cname = $_->{'NAME'};
            print $fileref "\"$name\" -> \"$cname\";\n";
        }
        
        # Now recursively treat each child.
        foreach (@$aref) {
            print_dot_node($fileref, $_);
        }
        
    }

}


# Print the header for the hg file.  The arguments are the file descriptor
# and the reference to the root node.  The name of the graph will be the 
# name of the root node (template).
sub print_hg_header($$) {
    my ($fileref, $nref) = @_;

    print $fileref <<"EOF"
<?xml version='1.0'?>
<!DOCTYPE GraphXML SYSTEM 'GraphXML.dtd'>
<GraphXML>
<graph id='".$nref->{'NAME'}."'>
<style>
<line tag='edge' class='include' colour='black' />
<fill tag='node' class='normal' colour='white' />
<line tag='node' class='normal' colour='blue' />
<fill tag='node' class='structure' colour='white' />
<line tag='node' class='structure' colour='gray' />
<fill tag='node' class='root' colour='blue' />
<line tag='node' class='root' colour='white' />
</style>
EOF
;

}

sub gen_hg_node($) {
    my ($nref) = @_;

    # Extract the node name and array of children.
    my $name = $nref->{'NAME'};
    my $aref = $nref->{'INCLUDES'};


    # Print the type of node. 
    my $type = $nref->{'TYPE'};

    # Add a node even if it has no children
    $hg_nodes.= "<node name='".$name."'  isDirected='True' class='".$type."'>
\t<label>".$name."</label>\n</node>\n";

    # If it has children, process them
    if (scalar(@$aref) > 0) {

        # Print the link to each one of the children.
        foreach (@$aref) {
            my $cname = $_->{'NAME'};
            $hg_edges.= "<edge source='".$name."' target='".$cname."' />\n";
        }
        
        # Now recursively process each child.
        foreach (@$aref) {
            gen_hg_node($_);
        }
        
    }

}


# Print a node into the hg file.  The arguments are the file descriptor 
# and the node reference.  Each node will print a shape definition and one
# edge for each child.  This will print nothing if the node has no children.
sub print_hg_node($$) {
    my ($fileref, $nref) = @_;

    # Extract the node name and array of children.
    my $name = $nref->{'NAME'};
    my $aref = $nref->{'INCLUDES'};

    # reset global variables
    $hg_nodes="";
    $hg_edges="";

    gen_hg_node($nref);

    print $fileref $hg_nodes;
    print $fileref $hg_edges;

}

# Print the footer for the hg file.  The argument is just the file
# descriptor.
sub print_hg_footer($) {
    my ($fileref) = @_;
    print $fileref "</graph>\n</GraphXML>";
}

sub usage() {

    print STDERR <<"EOF"

Generate include graphs from the panc log file.  The "call" 
logging option must have been used when running panc. Include 
graphs can be generated in dot format (default) or in the 
GraphXML format used by HyperGraph.

$0 [--help] [--format=dot|hg] {logfile}

EOF
;
    exit(1);
}

