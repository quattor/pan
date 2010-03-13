#
# @expect="/profile/result='true'"
#
object template include2;

variable FLAG = false;
include {'flag'}; 
'/result' = FLAG;
