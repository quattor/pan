#
# @expect="/profile/result='OK'"
#
object template ifexists2;

include { if_exists("x"); };

'/result' = {
  if (exists('/x')) {
    error('found template it should not have');
  };
  'OK';
};
