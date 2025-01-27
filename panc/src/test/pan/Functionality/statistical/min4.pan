#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template min4;

"/too_many_args" = min(123, 456, 789);
