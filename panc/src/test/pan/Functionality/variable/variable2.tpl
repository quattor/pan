#
# variables need to be initialised before they are used
# (and DML statements are independent)
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template variable2;

"/a" = foo = 1;
"/b" = foo;
