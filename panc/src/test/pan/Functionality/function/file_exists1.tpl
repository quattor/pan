#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template file_exists1;

'/result' = {
  x = '/absolute/file.txt';
  file_exists(x);
};

