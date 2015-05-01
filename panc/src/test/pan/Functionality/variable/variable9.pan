#
# test of global variable
#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#

object template variable9;

variable foo = "O";

variable foo = foo + "K";

"/result" = foo;
