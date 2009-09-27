#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template file_contents1;

'/result' = {
  x = '/absolute/file.txt';
  file_contents(x);
};

