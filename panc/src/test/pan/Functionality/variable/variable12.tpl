#
# test of the self variable
#
# @expect="/profile/test1=3 and /profile/test2=2.1 and /profile/test3='true' and /profile/test4='true'"
#

object template variable12;

function variable12_fun1 = SELF + 2;
function variable12_fun2 = exists(SELF);

"/test1" = 1;
"/test1" = variable12_fun1();

"/test2" = 0.1;
"/test2" = variable12_fun1();

"/test3" = variable12_fun2();

variable variable12_var = variable12_fun2();
"/test4" = variable12_var;
