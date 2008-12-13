#
# @expect=org.quattor.pan.exceptions.ValidationException
#
object template bug-trac-140-3;

bind '/result' = string(..3);

'/result' = 'oops';
