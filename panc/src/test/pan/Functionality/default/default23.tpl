#
# @expect="/profile/result/a='OK'"
#
object template default23;

type x = {
  'a' ? string = 'OK'
};

bind '/result' = x;

'/result/a' = undef;
