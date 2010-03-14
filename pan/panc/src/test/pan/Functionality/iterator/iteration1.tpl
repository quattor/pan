#
# @expect="/profile/result/a=1 and /profile/result/b='OK'"
#
object template iteration1;

variable X = nlist('a',1,'b','OK');

'/result' = {
  foreach(k; v; X) {
    r[k] = v;
  };
  r;
};
