#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template digest1;

'/result' = {
  a = 'MD2';
  r1 = digest(a, 'msg');
  r2 = digest('MD2', 'msg');
  r1 == r2;
};
