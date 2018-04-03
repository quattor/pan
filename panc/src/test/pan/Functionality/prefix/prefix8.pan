#
# make sure no invalid path is created by allowing relative prefixes
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template prefix8;

prefix '/';
'0' = 1;
