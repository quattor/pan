#
# @expect="count(/profile/result/*)=4 and /profile/result/a and /profile/result/b and /profile/result/c and /profile/result/d"
# @format=xmldb
#
object template reference3;

'/result' = {
  x = nlist("a",1,"b",2);
  y = x;
  y["c"] = x;
  x["d"] = 4;
  x;
};
