# Unit test for bug #7 in trac.
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template bug-trac-7;

'/result' = nlist('a',1,'a',2);
