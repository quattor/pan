#
# @expect="count(/profile/first/*)=2 and count(/profile/second/*)=3"
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
