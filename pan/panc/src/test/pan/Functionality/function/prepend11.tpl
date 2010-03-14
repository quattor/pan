#
# @expect="/profile/result='true'"
#
object template prepend11;

'/x' = prepend(n, 1);

'/result' = {
  x = value('/x');
  ((length(x) == 1) && (x[0] == 1));
};
