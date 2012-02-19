#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template default2; 

"/result" = "OK";
"/result" ?= "BAD";

