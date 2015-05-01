# 
# Using SELF as a function should result in a syntax error.
# See GitHub Issue #43.
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
object template bug-gh-43-1;

type f = string with match(SELF(foo));

'/a' = 1;
