#!/usr/bin/perl

my $size = 20000;

write_file('test-delete', "'/result' = null", $size);
write_file('test-assign', "'/result' = 1", $size);
write_file('test-include-null', "'/result' = null", 1, ' ');
write_file('test-include', "include test-include-null", $size);
write_file('test-cond-assign', "'/result' ?= 1", $size);

sub write_file {
    my ($tpl, $statement, $n, $obj) = @_;
    $obj ||= 'object';
    open TPL, '>', "$tpl.tpl";
    print TPL "$obj template $tpl;\n";
    for (my $i=0; $i<$n; $i++) {
	print TPL "$statement;\n";
    }
    close TPL;
}

