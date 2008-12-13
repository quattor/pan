#
# @expect="/profile/result='OK'"
#
object template default6; 

variable V1 = undef;
variable V1 ?= "OK";

"/result" = V1;
