#
# bad data given to base64_decode
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template function13;

"/bad" = base64_decode("a+b?c");
