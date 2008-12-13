#
# It must not be possible to set the FUNCTION
# variable.
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
object template function5;

'/result' = {
  FUNCTION = 'BAD';
};
