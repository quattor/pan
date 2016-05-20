# @expect=org.quattor.pan.exceptions.EvaluationException ".*invalid JSON value.*: End of input.*"

object template json5;

"/x" = json_decode('[[');
