#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template cross-validation1;

include { 'references_type' };
bind '/others' = references_type;

'/others' = nlist('cv2', 1);

'/result' = 'OK';

