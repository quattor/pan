#
# bogus regexps give a nice error message
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template regexp2;

"/bug" = match("12323", '^1[23+$');
