#
# sample file to test strings
#
# FIXME: The entity reference &#1 for \x00isn't properly read by the XML reader.
# FIXME: Make expectation do something.
#
# @expect="count(/nlist[@name='profile']/*)=9"
# @format=pan
#

object template strings1;

"/x0" = "";
"/x1" = "<foo>ok&ok</foo>";
"/x2" = "simple string";
"/x3" = "a\\b\tc\"";
"/x4" = "\x25\x6f\x6b\x21";
"/x5" = "line 1\nline 2\n";
"/x6" = "xxx\x25yyy";
"/x7" = <<EOT + "after";
 here 1
 here 2
EOT
"/x8" = "one \
two \n\
three";			# this is ok (but comments are forbidden after \)
