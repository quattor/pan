#
# check of escaped paths
#
# @expect="/nlist[@name='profile']/string[@name='a_2fb']='OK' and /nlist[@name='profile']/string[@name='alpha']='OK'"
# @format=pan
#

object template path1;

type x = {
  '{a/b}' : string
  'alpha' : string = 'OK'
};

bind '/' = x;

'/{a/b}' = 'OK';

