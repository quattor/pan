#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#

object template join1;

'/x1' = list("1", "2", "3");
'/x2' = join("-", value('/x1'));

'/result' = {
  x2 = value('/x2');
  res = "1-2-3";
  (x2 == res);
};
