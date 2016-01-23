#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template prepend2;

'/x' = list(1);

'/y' = prepend(value('/x'), 2);

'/result' = {
  x = value('/x');
  y = value('/y');
  (length(x) == 1 && length(y) == 2 && x[0] == 1 && y[0] == 2);
};

