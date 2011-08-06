#
# a resource can't be a record and a list
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template compat4;

"/bug/0" = 1; # /bug is now a list
"/bug/a" = 2; # /bug is used as a record
