#
# test of the self variable
#
# @expect="/nlist[@name='profile']/long[@name='test1']=3 and /nlist[@name='profile']/double[@name='test2']=2.1 and /nlist[@name='profile']/boolean[@name='test3']='true' and /nlist[@name='profile']/boolean[@name='test4']='true'"
# @format=pan
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
