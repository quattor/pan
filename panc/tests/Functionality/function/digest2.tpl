#
# @expect="/profile/result='true'"
#
object template digest2;

'/result' = {
  a = 'MD5';
  r1 = digest(a, 'msg');
  r2 = digest('MD5', 'msg');
  r1 == r2;
};
