#
# @expect=org.quattor.pan.exceptions.SyntaxException ".*validate\(\) requires exactly two arguments.*"
#

object template validate11;

'/res' = validate(1, 2, 3);
