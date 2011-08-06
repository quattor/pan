#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template default5; 

"/test" = "OK";
final "/test" ?= "BAD";
"/test" = "REALLY BAD";
