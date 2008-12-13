# This template should produce a traceback where
# the source location of the function definition
# is correct.
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug-trac-70;

function f = {
  ARGV['xxx'];
};

'/result' = f(10);
