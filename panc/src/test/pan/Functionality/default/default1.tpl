#
# @expect="/profile/test='OK'"
#
object template default1; 

"/test" = undef;
"/test" ?= "OK";

