#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template bug-trac-89-1;

include { if_exists('ns/x') };

'/result' = {
  if (exists('/x')) {
    error('found template it should not have');
  };
  'OK';
};
