#
# @expect="/profile/result='OK'"
#
# Partially tests SF bug #2860089.
#
object template value1;

'/result' = value('ns/profile:/foo');
