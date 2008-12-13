#
# @expect="/profile/result='false'"
#
object template exists10;

# Check that using exists on the same path that is being
# set always returns true.

'/x' ?= exists(X);

'/y' = undef;
'/y' ?= exists(Y);

'/z' = null;
'/z' ?= exists(Z);

'/result' = value('/x') && value('/y') && value('/z');
