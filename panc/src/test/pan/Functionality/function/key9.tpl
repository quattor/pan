#
# test of first/next like in the documentation
#
# @expect="/profile/ex1=15 and /profile/ex2[1]='a' and /profile/ex2[2]='b' and /profile/ex2[3]='c'"
# @format=xmldb
#

object template key9;

"/ex1" = {
  numlist = list(1, 2, 4, 8);
  sum = 0;
  ok = first(numlist, k, v);
  while (ok) {
    sum = sum + v;
    ok = next(numlist, k, v);
  };
  return(sum);
};

"/ex2" = {
  table = nlist("a", 1, "b", 2, "c", 3);
  keys = list();
  ok = first(table, k, v);
  while (ok) {
    keys[length(keys)] = k;
    ok = next(table, k, v);
  };
  return(keys);
};
