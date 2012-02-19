#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
# Partially tests SF bug #2860089.
#
object template value1;

'/result' = value('ns/profile:/foo');
