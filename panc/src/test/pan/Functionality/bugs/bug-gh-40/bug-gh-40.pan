# 
# The error() function should give the name of the file
# where the error was raised, not the object template.
#
# See GitHub Issue #40.
#
# @expect=org.quattor.pan.exceptions.EvaluationException ".*c\.pan.*c\.pan.*"
#
object template bug-gh-40;
include 'b';

