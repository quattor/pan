#
# test of variable child
#
# @expect="/profile/a[1]='OK' and /profile/x1[1]='OK' and /profile/x2='OK' and /profile/x3/a[1]='OK'"
# @format=xmldb
#

object template variable3;

"/a/0" = "OK";

"/x1" = {
  v1 = value("/");
  v1["a"];
};

"/x2" = {
  v2 = value("/");
  v2["a"][0];
};

"/x3" = {
  v3["a"][0] = "OK";
  v3;
};
