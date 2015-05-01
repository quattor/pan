#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template file_contents3;

'/result' = file_contents('nonexistant.txt');
