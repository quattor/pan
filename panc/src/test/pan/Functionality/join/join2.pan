#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#

object template join2;

'/x' = join("-", "1", "2", "3");

'/result' = {
  x = value('/x');
  res = "1-2-3";
  (x == res);
};
