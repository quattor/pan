# 
# The error message for a non-constant default value
# must contain the line number in the error message.
#
# See GitHub Issue #37.
#
# @expect=org.quattor.pan.exceptions.SyntaxException ".*bug-gh-37\.tpl:12\.18\-12\.28.*"
#
object template bug-gh-37;

type t = {
    "a" : long = value("/a")
};
