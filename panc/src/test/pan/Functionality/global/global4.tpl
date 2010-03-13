#
# verify that global variables cannot be modified indirectly
# through local variables
#
# @expect="/profile/result='true' and /profile/result2='true'"
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
