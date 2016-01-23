#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template default6; 

variable V1 = undef;
variable V1 ?= "OK";

"/result" = V1;
