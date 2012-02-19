#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template default8; 

variable V1 ?= "OK";

"/result" = V1;
