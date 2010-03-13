#
# @expect="/profile/result='true'"
#
object template delete8;

'/removed' = 1;
'/removed' = null;
'/result' = !exists('/removed');
