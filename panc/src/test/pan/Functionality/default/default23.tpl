#
# @expect="/profile/result/a='OK'"
# @format=xmldb
#
object template default23;

type x = {
  'a' ? string = 'OK'
};

bind '/result' = x;

'/result/a' = undef;
