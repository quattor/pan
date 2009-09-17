#
# @expect="/profile/result='false'"
#
# Partially tests SF bug #2860089.
#
object template exists11;

'/result' = exists('ns/profile:/foo');
