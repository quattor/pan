#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template file_contents5;

'/result' = {
  file_contents("afile", "abc");
};
