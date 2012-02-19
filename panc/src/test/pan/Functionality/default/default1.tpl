#
# @expect="/nlist[@name='profile']/string[@name='test']='OK'"
# @format=pan
#
object template default1; 

"/test" = undef;
"/test" ?= "OK";

