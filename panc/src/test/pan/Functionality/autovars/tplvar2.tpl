#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template tplvar2;

'/result' = {
  if (!exists(TEMPLATE)) {
    error('TEMPLATE variable does not exist');
  };
  if (TEMPLATE != 'tplvar2') {
    error('TEMPLATE variable has incorrect value');
  };
  'OK';
};
