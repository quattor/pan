#
# @expect="/profile/test='OK'"
# @format=xmldb
#
object template default1; 

"/test" = undef;
"/test" ?= "OK";

