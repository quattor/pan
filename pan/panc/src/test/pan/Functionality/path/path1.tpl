#
# check of escaped paths
#
# @expect="/profile/a_2fb='OK' and /profile/alpha='OK'"
#

object template path1;

type x = {
  '{a/b}' : string
  'alpha' : string = 'OK'
};

bind '/' = x;

'/{a/b}' = 'OK';

