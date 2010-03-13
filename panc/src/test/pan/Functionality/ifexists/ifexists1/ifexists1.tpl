#
# @expect="/profile/result='OK'"
#
object template ifexists1;

include { if_exists('x'); };

'/result' = {
  if (!exists('/x')) {
    error('didn''t include template');
  };
  'OK';
};

