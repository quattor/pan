#
# @expect="/profile/result='OK'"
#
object template default7; 

variable V1 = "OK";
variable V1 ?= "BAD";

"/result" = V1;
