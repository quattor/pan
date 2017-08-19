#
# invalid path
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template path15;

# leading 0s for negative index, even if they are valid octals, are invalid path terms
"/foo" = list(0, 1);
"/foo/-01" = 2;
