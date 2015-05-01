#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template delete8;

'/removed' = 1;
'/removed' = null;
'/result' = !exists('/removed');
