#
# undefined elements can't appear in XML
#
# @expect=org.quattor.pan.exceptions.ValidationException ".*element at /x is undefined.*"
#

object template undef1;

"/x" = undef;
