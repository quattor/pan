#
# @expect="count(/profile/result2)-count(/profile/result1)=1"
# @format=xmldb
# @format=xmldb
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
