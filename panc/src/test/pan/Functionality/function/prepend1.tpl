#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template prepend1;

'/x' = {
  x[0] = 1;
  prepend(x, 2);
};

'/result' = {
  x = value('/x');
  (length(x) == 2 && x[0] == 2);
};

