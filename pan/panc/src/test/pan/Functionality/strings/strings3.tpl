#
# single quoted strings must be properly terminated
#
# @expect=org.quattor.pan.parser.ParseException
#

template bad;

"/x1" = 'foo'';
