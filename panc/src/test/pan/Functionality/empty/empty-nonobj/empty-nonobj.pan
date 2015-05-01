#
# Try various empty templates.  If this compiles, everything's OK.
#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template empty-nonobj;

include {'empty4'};
include {'empty5'};
'/dummy' = create('empty6');

'/result' = 'OK';

