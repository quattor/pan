#
# @expect="/profile/result[1]=1 and /profile/result[2]='OK'"
# @format=xmldb
#
object template iteration4;

variable X = list(1,'OK');

'/result' = {
  x = X;
  ok = first(x, k, v);
  while (ok) {
    r[k] = v;
    ok = next(x, k, v);
  };
  r;
};
