#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
object template prepend9;

'/x' = prepend('BAD', 1);
