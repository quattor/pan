#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template append4;

'/x' = list(1);

'/x' = append(2);

'/result' = {
  x = value('/x');
  (length(x) == 2 && x[0] == 1 && x[1] == 2);
};

