#
# failed type validation
#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template validation3;

type uint = long(0..);
bind "/x" = uint[];

"/x" = list(1, -1);
