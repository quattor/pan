#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#

object template join1;

'/x1' = list("1", "2", "3");
'/x2' = join("-", value('/x1'));

'/result' = {
  res = value('/x2');
  exp = "1-2-3";
  (exp == res);
};
