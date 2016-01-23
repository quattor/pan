#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug10406-2;

"/result" = {
  lst = list("a","b","c","d");
  ok = next(lst,k,v);
};
