# @expect=org.quattor.pan.exceptions.EvaluationException ".*undef cannot be converted to JSON.*"

object template json8;

"/x" = json_encode(nlist("a", undef));
