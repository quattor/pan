#
# @expect="/profile/result='OK'"
#
object template function1;

'/result' = {
  if (exists(FUNCTION)) {
    error('FUNCTION variable exists but should not');
  };
  'OK';
};
