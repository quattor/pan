#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template final5;

final "/test" = nlist("one",nlist("a",1),"two",2);

"/test/one/a" = 3;

