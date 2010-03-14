#
# @expect="/profile/result='OK'"
#
object template function4;

# Check that FUNCTION value is correctly cleared.
function BAD = FUNCTION;

'/result' = {
  BAD();
  if (exists(FUNCTION)) {
    error('FUNCTION variable exists but should not');
  };
  'OK';
};
