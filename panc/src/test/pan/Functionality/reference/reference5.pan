#
# @expect="count(/nlist[@name='profile']/nlist[@name='result']/nlist[@name='a']/*)=2"
# @format=pan
#
object template reference5;

'/result' = {
  x = nlist("a",nlist('aa','2'),"b",2);
  y = x["a"];
  y["bb"] = x;
  x;
};
