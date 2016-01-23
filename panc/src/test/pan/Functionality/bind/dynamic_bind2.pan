#
# @expect=org.quattor.pan.exceptions.ValidationException
#
object template dynamic_bind2;

variable X = 'bad';

bind '/result/${X}' = long;

'/result/bad' = 'BAD';

