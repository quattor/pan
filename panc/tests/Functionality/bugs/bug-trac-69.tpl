# This template should produce a traceback that includes
# the location of the function definition.
#
# @expect="/profile/result='OK'"
#
object template bug-trac-69;

function f = {
  traceback('forced traceback');
  'OK';
};

'/result' = f();
