#
# test of return pseudo-function
#
# @expect="/profile/x1='ok3' and /profile/x2='ok'"
#

object template return1;

function foo = {
  if (1 > 0.999)
    return(list(1, 2, 3));
  # hopefully not reached
  "very bad";
};

"/x1" = {
  return("ok" + to_string(length(foo())));
  # not reached
  "bad";
};

"/x2" = {
  0 > -1 && return("ok");
  return("bad");
};
