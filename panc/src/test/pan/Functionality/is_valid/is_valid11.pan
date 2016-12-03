#
# @expect=org.quattor.pan.exceptions.SyntaxException ".*is_valid\(\) requires exactly two arguments.*"
#

object template is_valid11;

'/res' = is_valid(1, 2, 3);
