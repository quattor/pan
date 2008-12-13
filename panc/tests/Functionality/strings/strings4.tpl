#
# no multiline string (this way)
#
# @expect=org.quattor.pan.parser.ParseException
#

template bad;

"/x" = "line 1
line 2";
