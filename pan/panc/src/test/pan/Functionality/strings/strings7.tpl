#
# invalid escape sequence
#
# @expect=org.quattor.pan.parser.ParseException
#

template bad;

"/x" = "\xx";
