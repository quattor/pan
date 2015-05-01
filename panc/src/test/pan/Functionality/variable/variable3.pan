#
# test of variable child
#
# @expect="/nlist[@name='profile']/list[@name='a']/*[1]='OK' and /nlist[@name='profile']/list[@name='x1']/*[1]='OK' and /nlist[@name='profile']/string[@name='x2']='OK' and /nlist[@name='profile']/nlist[@name='x3']/list[@name='a']/*[1]='OK'"
# @format=pan
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
