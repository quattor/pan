#
# @expect="count(/profile/result/*)=2 and /profile/result/_782b79 and /profile/result/_786d6c"
#
object template encode1;

"/result" = nlist("xml","one","x+y","two");
