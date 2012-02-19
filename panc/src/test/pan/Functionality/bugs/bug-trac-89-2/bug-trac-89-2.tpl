#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template bug-trac-89-2;

include { if_exists('ns/x') };

'/result' = {
  if (!exists('/x')) {
    error('did not find template it should have');
  };
  'OK';
};
