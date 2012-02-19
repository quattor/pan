#
# Check that iteration over a resource works.
#
# @expect="/profile/result=5"
# @format=xmldb
#
object template forloop4;

'/result' = {
  x = list(1, 2, 3, 4, 5);
  for (ok = first(x, k, v); ok; ok = next(x, k, v)) {
    v;
  };
};
