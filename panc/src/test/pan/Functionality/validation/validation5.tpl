#
# failed validation via error()
#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template validation5;

valid "/x" = SELF > 7 || error("too small");
"/x" = 6;
