#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template bug-trac-81-1;

'/result' = {
  if (exists('ns/x')) {
    error('found template it should not have');
  };
  'OK';
};
