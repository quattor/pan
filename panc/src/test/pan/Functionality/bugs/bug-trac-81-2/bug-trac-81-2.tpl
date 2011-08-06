#
# @expect="/profile/result='OK'"
#
object template bug-trac-81-2;

'/result' = {
  if (!exists('ns/x')) {
    error('did not find template it should have');
  };
  'OK';
};
