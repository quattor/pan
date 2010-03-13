#
# make sure that escaped literal paths refer to the correct things
#
# @expect="/profile/result='OK'"
#

object template path2;

'/x' = nlist(escape('a/b'), 'OK');
'/result' = value('/x/{a/b}');

