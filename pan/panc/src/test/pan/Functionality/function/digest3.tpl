#
# @expect="/profile/result='true'"
#
object template digest3;

'/result' = {
  a = 'SHA';
  r1 = digest(a, 'msg');
  r2 = digest('SHA', 'msg');
  r1 == r2;
};
