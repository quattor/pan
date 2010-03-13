#
# @expect="/profile/result='OK'"
#
object template bug-trac-89-1;

include { if_exists('ns/x') };

'/result' = {
  if (exists('/x')) {
    error('found template it should not have');
  };
  'OK';
};
