#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template include4;

include {undef}; 
'/result' = true;
