#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template key3;
"/x/a" = 1;
"/x/b" = 1;

"/y" = key (value("/x"),value("/x"));
