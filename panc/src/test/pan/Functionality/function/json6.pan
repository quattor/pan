# @expect=org.quattor.pan.exceptions.EvaluationException ".*JSON path '\$\.a\.0' is not a valid PAN path.*"

object template json6;

"/x" = json_decode('{"a": {"0": "b"}}');
