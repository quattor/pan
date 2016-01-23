#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template prepend3;

'/x' = list(1);

'/x' = prepend(SELF, 2);

'/result' = {
  x = value('/x');
  (length(x) == 2 && x[1] == 1 && x[0] == 2);
};

