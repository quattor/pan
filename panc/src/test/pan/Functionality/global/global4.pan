#
# verify that global variables cannot be modified indirectly
# through local variables
#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true' and /nlist[@name='profile']/boolean[@name='result2']='true' "
# @format=pan
#
object template global4;

variable GLOBAL = nlist('a', 1, 'b', nlist('c', 3, 'd', 4));

'/result' = {
  x = GLOBAL;
  y = x['b'];
  y['e'] = 5;
  exists(x['b']['e']);
};

'/result2' = !exists(GLOBAL['b']['e']);
