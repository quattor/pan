#
# list and nlist are incompatible in assignments
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template compat8;

"/list" = list(1, 2);
"/list" = nlist("a", "b");
