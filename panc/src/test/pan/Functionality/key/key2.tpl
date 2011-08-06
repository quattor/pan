#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template key2;
"/x/a" = 1;
"/x/b" = 2;

"/y" = key (value("/x"),0);
"/z" = key (value("/x"),2);