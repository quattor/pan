#
# show a nasty bug with reference counting :-(
#
# @expect="/nlist[@name='profile']/nlist[@name='result']/list[@name='key']/*[1]='aaa' and /nlist[@name='profile']/nlist[@name='result']/list[@name='key']/*[2]='bbb'"
# @format=pan
#

object template weird6;

function weird6_fun = {
    ARGV[0][1] = "bbb";
    return(ARGV[0]);
};

"/result" = {
  var["key"][0] = "aaa";
  var["key"] = weird6_fun(var["key"]);
  return(var);
};
