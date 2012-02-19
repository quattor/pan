#
# make sure that escaped literal paths refer to the correct things
#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#

object template path13;

'/{}' = 'OK';
'/result' = value('/{}');

