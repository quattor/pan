#
# references to the same variable must give back consistent value
# even if the value is protected() when being referenced
#
# @expect="/profile/result='OK'"
#

object template variable14;

variable X = list(1, 2, 3);

'/result' = {
  for (ok = first(X, k, v); ok; ok = next(X, k, v)) {
    debug(to_string(v));
  };
  'OK';
};
