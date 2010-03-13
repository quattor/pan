#
# @expect="/profile/result='OK'"
#
object template bug-trac-81-1;

'/result' = {
  if (exists('ns/x')) {
    error('found template it should not have');
  };
  'OK';
};
