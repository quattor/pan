#
# @expect="count(/nlist[@name='profile']/list[@name='result2']/*)-count(/nlist[@name='profile']/list[@name='result1']/*)=1"
# @format=pan
#
object template loadpath1;

"/result1" = LOADPATH;

variable LOADPATH = {
  lst = SELF;
  size = length(lst);
  lst[size] = "added/path";
  return(lst);
};

"/result2" = LOADPATH;
