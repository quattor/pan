#
# invalid type
#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template types4;

bind "/x" = long[];
"/x/0" = "1";
