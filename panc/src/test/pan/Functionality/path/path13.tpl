#
# make sure that escaped literal paths refer to the correct things
#
# @expect="/profile/result='OK'"
#

object template path13;

'/{}' = 'OK';
'/result' = value('/{}');

