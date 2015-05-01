#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template tplvar1;

'/result' = {
  if (!exists(TEMPLATE)) {
    error('TEMPLATE variable does not exist');
  };
  'OK';
};
