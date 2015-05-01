#
# It must not be possible to set the TEMPLATE
# variable.
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
object template tplvar4;

'/result' = {
  TEMPLATE = 'BAD';
};
