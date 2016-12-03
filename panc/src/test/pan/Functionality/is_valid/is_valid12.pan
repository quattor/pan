#
# @expect=org.quattor.pan.exceptions.SyntaxException ".*is_valid\(\) requires the first argument to be a type.*"
#

object template is_valid12;

'/res' = is_valid("type", "variable");
