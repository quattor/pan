#
# test of delete as pseudo function
#
# @expect="count(/profile/list)=2 and /profile/list[1]='aa' and /profile/list[2]='dd' and count(/profile/nlist/*)=2 and /profile/nlist/xx=1 and /profile/nlist/zz=3"
# @format=xmldb
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
