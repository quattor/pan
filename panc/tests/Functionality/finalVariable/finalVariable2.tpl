#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template finalVariable2;

# Final variables cannot be modified after being set.
# The final flag should be set on a value even if the
# conditional value isn't used.

variable X = 1;
final variable X ?= 2;
variable X = 3;

'/result' = 'FAIL';

