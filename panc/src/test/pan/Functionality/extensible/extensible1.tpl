#
# @expect=org.quattor.pan.exceptions.ValidationException
#
object template extensible1;

type type_info = {
  "alpha" : string
  "beta"  ? string
};

bind "/fails" = type_info;

"/fails" = nlist("alpha", "a", "beta", "b", "gamma", "c");
