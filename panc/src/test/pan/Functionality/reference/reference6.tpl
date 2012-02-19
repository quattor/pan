#
# @expect="count(/profile/result/*)=0"
# @format=xmldb
#
object template reference6;

variable X = nlist();

'/result' = {
  x = X;
  x['a'] = "BUG";
  X;
};
