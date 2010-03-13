#
# @expect="/profile/result='OK'"
#
object template default2; 

"/result" = "OK";
"/result" ?= "BAD";

