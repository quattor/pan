#
# references to the same variable must give back consistent value
# even if the value is protected() when being referenced
#
# @expect="/profile/result='OK'"
#

object template variable13;

variable X = list(1, 2, 3);

'/result' = {
  ok = first(X, k, v);
  while (ok) {
    debug(to_string(v));
    ok = next(X, k, v);
  };
  'OK';
};

