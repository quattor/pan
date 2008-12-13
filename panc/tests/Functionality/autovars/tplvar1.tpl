#
# @expect="/profile/result='OK'"
#
object template tplvar1;

'/result' = {
  if (!exists(TEMPLATE)) {
    error('TEMPLATE variable does not exist');
  };
  'OK';
};
