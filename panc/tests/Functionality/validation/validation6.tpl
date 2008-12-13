#
# validation code is not allowed to modify self
#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template validation6;

valid "/test" = {
  SELF[0] = 1;
  return(true);
};

"/test/0" = 0;
