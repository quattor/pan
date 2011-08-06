#
# test of global variable
#
# @expect="/profile/result='OK'"
#

object template variable9;

variable foo = "O";

variable foo = foo + "K";

"/result" = foo;
