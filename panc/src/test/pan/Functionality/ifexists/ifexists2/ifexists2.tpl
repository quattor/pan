#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template ifexists2;

include { if_exists("x"); };

'/result' = {
  if (exists('/x')) {
    error('found template it should not have');
  };
  'OK';
};
