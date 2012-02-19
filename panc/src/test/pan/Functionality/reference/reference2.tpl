#
# @expect="count(/profile/result/*)=3"
# @format=xmldb
#
object template reference2;

'/result' = {
  x = nlist("a",1,"b",2);
  y = x;
  y["c"] = 3;
  x;
};
