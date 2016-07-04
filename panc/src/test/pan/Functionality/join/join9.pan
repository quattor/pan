#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template join9;

'/x' = join(1, "a", "b", "c");
