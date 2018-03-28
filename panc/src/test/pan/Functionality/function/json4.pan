# @expect=org.quattor.pan.exceptions.EvaluationException ".*invalid JSON value.*Expected name.*"

object template json4;

"/x" = json_decode('{[{');
