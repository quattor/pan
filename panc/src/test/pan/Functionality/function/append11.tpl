#
# @expect="/profile/result='true'"
#
object template append11;

'/x' = append(n, 1);

'/result' = {
  x = value('/x');
  ((length(x) == 1) && (x[0] == 1));
};
