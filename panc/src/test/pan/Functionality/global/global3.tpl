#
# verify that global variables cannot be modified indirectly
# through local variables
#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template global3;

variable GLOBAL = nlist();

'/x' = {
  x = GLOBAL;
  x['bad'] = true;
  'OK';
};

'/result' = !exists(GLOBAL['bad']);
