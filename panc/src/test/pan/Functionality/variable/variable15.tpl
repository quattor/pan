#
# references to the same variable must give back consistent value
# even if the value is protected() when being referenced
#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#

object template variable15;

variable X = list(1, 2, 3);

'/result' = {
  foreach (k; v; X) {
    debug(to_string(v));
  };
  'OK';
};
