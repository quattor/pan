#
# to_long() can over/uder-flow
#

object template function11;

"/bad" = to_long(1e99);
