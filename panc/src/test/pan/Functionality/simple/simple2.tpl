#
# test data manipulation language constructs
#
# @expect="/nlist[@name='profile']/long[@name='x1']=3 and /nlist[@name='profile']/long[@name='x2']=3 and /nlist[@name='profile']/string[@name='x3']='ok' and /nlist[@name='profile']/string[@name='x4']='ok' and /nlist[@name='profile']/long[@name='x5']=8 and /nlist[@name='profile']/long[@name='x6']=720"
# @format=pan
#

object template simple2;

"/x1" = 1 + 2;

"/x2" = {
  1 + 2;
};

"/x3" = if (true) "ok" else "bad";

"/x4" = {
  x = if (true) "ok" else "bad";
  x;
};

"/x5" = {
  x = 7;
  x = x + 1;
  x;
};

"/x6" = {
  n = result = 1;
  while (n <= 6) {
    result = result * n;
    n = n + 1;
  };
  result; # should be 6!, i.e. 720
};
