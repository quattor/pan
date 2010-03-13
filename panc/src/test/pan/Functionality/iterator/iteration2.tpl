#
# @expect="/profile/result/a=1 and /profile/result/b='OK'"
#
object template iteration2;

variable X = nlist('a',1,'b','OK');

'/result' = {
  x = X;
  ok = first(x, k, v);
  while (ok) {
    r[k] = v;
    ok = next(x, k, v);
  };
  r;
};
