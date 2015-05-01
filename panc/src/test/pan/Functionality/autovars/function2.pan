#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template function2;

function OK = FUNCTION;

'/result' = {
  OK();
};
