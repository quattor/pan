#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template default9; 

variable V = "OK";
final variable V ?= "BAD";
variable V = "REALLY BAD";

"/result" = V;
