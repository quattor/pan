#
# @expect="count(/profile/result[1]/*)=2 and count(/profile/result[2]/*)=2"
# @format=xmldb
#
object template reference4;

'/result' = {
  x = nlist("a",1,"b",2);
  y[0] = x;
  y[1] = x;
  x["c"] = 3;
  y;
};
