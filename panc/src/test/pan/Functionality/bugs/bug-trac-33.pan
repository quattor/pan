#
# @expect=org.quattor.pan.exceptions.ValidationException
#
object template bug-trac-33;
type x = string;
bind '/a/b' = x;
