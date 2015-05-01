#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template include2;

variable FLAG = false;
include {'flag'}; 
'/result' = FLAG;
