#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template digest6;

'/result' = {
  a = 'SHA-384';
  r1 = digest(a, 'msg');
  r2 = digest('SHA-384', 'msg');
  r1 == r2;
};
