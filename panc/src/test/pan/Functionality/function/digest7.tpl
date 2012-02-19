#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template digest7;

'/result' = {
  a = 'SHA-512';
  r1 = digest(a, 'msg');
  r2 = digest('SHA-512', 'msg');
  r1 == r2;
};
