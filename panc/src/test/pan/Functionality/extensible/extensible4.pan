#
# @expect="count(/nlist[@name='profile']/nlist[@name='third']/*)=2"
# @format=pan
#
object template extensible4;

type type_info1 = {
  "alpha" : string
};

type type_info2 = {
  "beta"  : string
};

type type_info3 = {
  include type_info1
  include type_info2
};

bind "/third" = type_info3;

"/third" = nlist("alpha", "a", "beta", "b");
