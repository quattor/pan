#
# @expect="count(/profile/result/a/*)=2"
#
object template reference5;

'/result' = {
  x = nlist("a",nlist('aa','2'),"b",2);
  y = x["a"];
  y["bb"] = x;
  x;
};
