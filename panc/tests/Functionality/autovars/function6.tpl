#
# It must not be possible to set the FUNCTION
# variable.
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
object template function6;

variable FUNCTION = 'BAD';
