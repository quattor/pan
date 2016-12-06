#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#

object template join2;

'/x' = join("-", "1", "2", "3");

'/result' = {
  res = value('/x');
  exp = "1-2-3";
  (exp == res);
};
