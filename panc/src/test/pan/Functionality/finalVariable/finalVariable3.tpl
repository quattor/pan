#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template finalVariable3;

# Final variables cannot be modified after being set.
# The final variable flag should not be cleared if a 
# conditional variable statement is called after the 
# final flag is set.

final variable X = 1;
variable X ?= 2;
variable X = 3;

'/result' = 'FAIL';

