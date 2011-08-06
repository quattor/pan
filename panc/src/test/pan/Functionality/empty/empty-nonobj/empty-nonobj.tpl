#
# Try various empty templates.  If this compiles, everything's OK.
#
# @expect="/profile/result='OK'"
#
object template empty-nonobj;

include {'empty4'};
include {'empty5'};
'/dummy' = create('empty6');

'/result' = 'OK';

