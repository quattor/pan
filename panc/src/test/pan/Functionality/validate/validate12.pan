#
# @expect=org.quattor.pan.exceptions.SyntaxException ".*validate\(\) requires the first argument to be a type.*"
#

object template validate12;

'/res' = validate("type", "variable");
