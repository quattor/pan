#
# @expect="count(/nlist[@name='profile']/nlist[@name='first']/*)=2 and count(/nlist[@name='profile']/nlist[@name='second']/*)=3"
# @format=pan
#
object template extensible2;

type type_info = extensible {
  "alpha" : string
  "beta"  ? string
};

bind "/first" = type_info;
bind "/second" = type_info;

"/first" = nlist("alpha", "a", "beta", "b");
"/second" = nlist("alpha", "a", "beta", "b", "gamma", "c");
