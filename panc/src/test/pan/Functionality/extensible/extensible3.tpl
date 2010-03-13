#
# @expect=org.quattor.pan.exceptions.ValidationException
#
object template extensible3;

type type_info1 = extensible {
  "alpha" : string
  "beta"  ? string
};

type type_info2 = extensible {
  "alpha" ? string
  "beta"  : string
};

type type_info3 = {
  include type_info1
  include type_info2
};

bind "/third" = type_info3;

"/third" = nlist("alpha", "a", "beta", "b", "gamma", "c");
