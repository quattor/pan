#
# Check that infinite loop throws an exception. 
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template forloop3;

'/result' = {
  for (undef; true; undef) {
    undef;
  };
};
