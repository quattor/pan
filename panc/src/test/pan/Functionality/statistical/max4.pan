#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template max4;

"/too_many_args" = max(123, 456, 789);
