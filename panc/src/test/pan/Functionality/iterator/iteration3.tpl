#
# @expect="/profile/result[1]=1 and /profile/result[2]='OK'"
# @format=xmldb
#
object template iteration3;

variable X = list(1,'OK');

'/result' = {
  foreach(k; v; X) {
    r[k] = v;
  };
  r;
};
