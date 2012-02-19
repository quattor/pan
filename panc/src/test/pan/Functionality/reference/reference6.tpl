#
# @expect="count(/nlist[@name='profile']/nlist[@name='result']/*)=0"
# @format=pan
#
object template reference6;

variable X = nlist();

'/result' = {
  x = X;
  x['a'] = "BUG";
  X;
};
