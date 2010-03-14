#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template finalVariable1;

# Final variables cannot be modified after being set.
# This template should fail during execution. 

final variable X = 1;
variable X = 2;

'/result' = 'FAIL';

