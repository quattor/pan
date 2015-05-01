#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug10406-1;

"/result" = {
  nlst = list("a","b","c","d");
  ok = next(nlst,k,v);
};
