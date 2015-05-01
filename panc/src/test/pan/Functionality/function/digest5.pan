#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template digest5;

'/result' = {
  a = 'SHA-256';
  r1 = digest(a, 'msg');
  r2 = digest('SHA-256', 'msg');
  r1 == r2;
};
