#
# undefined elements can't appear in XML
#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template undef1;

"/x" = undef;
