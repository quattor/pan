#
# Local variables inside a functional context must not affect 
# the value of local variables outside of the function.
#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template localvars;

function set_value = {
  i = 10;
};

'/result' = {
 i = 1;
 set_value();
 if (i==10) {
   error("side effect on local variable from functional context");
 };
 'OK';
};

