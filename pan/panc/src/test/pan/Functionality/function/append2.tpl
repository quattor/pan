#
# @expect="/profile/result='true'"
#
object template append2;

'/x' = list(1);

'/y' = append(value('/x'), 2);

'/result' = {
  x = value('/x');
  y = value('/y');
  (length(x) == 1 && length(y) == 2 && x[0] == 1 && y[1] == 2);
};

