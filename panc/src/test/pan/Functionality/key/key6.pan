#
# incorrect use of first/next
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template key6;

"/bug" = first(nlist(), 2, 3);
