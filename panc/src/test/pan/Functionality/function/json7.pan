# @expect=org.quattor.pan.exceptions.EvaluationException ".*undef cannot be converted to JSON.*"

object template json7;

"/x" = json_encode(undef);
