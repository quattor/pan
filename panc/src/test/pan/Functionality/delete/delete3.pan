#
# test of delete as pseudo function
#
# @expect="count(/nlist[@name='profile']/list[@name='list']/*)=2 and /nlist[@name='profile']/list[@name='list']/*[1]='aa' and /nlist[@name='profile']/list[@name='list']/*[2]='dd' and count(/nlist[@name='profile']/nlist[@name='nlist']/*)=2 and /nlist[@name='profile']/nlist[@name='nlist']/long[@name='xx']=1 and /nlist[@name='profile']/nlist[@name='nlist']/long[@name='zz']=3"
# @format=pan
#

object template delete3;

"/list" = {
  x = list("aa", "bb", "cc", "dd");
  delete(x[1]);
  delete(x[1]);
  return(x);
};

"/nlist" = {
  x["xx"] = 1;
  x["yy"] = 2;
  x["zz"] = 3;
  delete(x["yy"]);
  delete(x["yy"]);
  return(x);
};
