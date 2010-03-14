#
# @expect="/profile/result='OK'"
#
object template variable11;

variable variable11_var = nlist("key", "OK");

function variable11_fun = {
  x = variable11_var["key"];
  return(x);
};

"/result" = variable11_fun();
