#
# Check that iteration over a resource works.
#
# @expect="/nlist[@name='profile']/long[@name='result']=5"
# @format=pan
#
object template forloop4;

'/result' = {
  x = list(1, 2, 3, 4, 5);
  for (ok = first(x, k, v); ok; ok = next(x, k, v)) {
    v;
  };
};
