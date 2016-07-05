#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template join6;

'/x2' = join(',', dict("key1", 1, "key2", 2));
