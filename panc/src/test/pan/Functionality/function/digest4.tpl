#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template digest4;

'/result' = {
  a = 'SHA-1';
  r1 = digest(a, 'msg');
  r2 = digest('SHA-1', 'msg');
  r1 == r2;
};
