#
# @expect="count(/nlist[@name='profile']/nlist[@name='third']/*)=2"
# @format=pan
#
object template extensible5;

type type_info1 = {
  "alpha" : string
};

type type_info3 = extensible {
  include type_info1
  "beta" ? long
};

bind "/third" = type_info3;

"/third" = nlist("alpha", "1", "gamma", true);
