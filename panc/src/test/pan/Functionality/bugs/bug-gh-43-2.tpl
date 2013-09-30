# 
# Using SELF as a function should result in a syntax error.
# See GitHub Issue #43.
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
object template bug-gh-43-2;

'/a' = SELF(10);
