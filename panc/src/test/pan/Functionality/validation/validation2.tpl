#
# failed validation
#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template validation2;

valid "/x" = SELF > 2;

"/x" = 1;
