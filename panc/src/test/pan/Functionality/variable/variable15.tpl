#
# references to the same variable must give back consistent value
# even if the value is protected() when being referenced
#
# @expect="/profile/result='OK'"
#

object template variable15;

variable X = list(1, 2, 3);

'/result' = {
  foreach (k; v; X) {
    debug(to_string(v));
  };
  'OK';
};
