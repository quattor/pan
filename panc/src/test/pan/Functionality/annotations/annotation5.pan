#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
@(description with a=b)
object template annotation5;

@{  key_spaces_before_equal   = value with a=b}
'/result' = 'OK';
