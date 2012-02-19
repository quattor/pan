#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template function1;

'/result' = {
  if (exists(FUNCTION)) {
    error('FUNCTION variable exists but should not');
  };
  'OK';
};
