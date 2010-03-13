#
# @expect="/profile/result='OK'"
#
object template bug-trac-89-2;

include { if_exists('ns/x') };

'/result' = {
  if (!exists('/x')) {
    error('did not find template it should have');
  };
  'OK';
};
