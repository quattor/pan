#
# invalid path
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template path14;

# leading 0s, even if they are valid octals, are invalid path terms
"/foo/06" = 0;
