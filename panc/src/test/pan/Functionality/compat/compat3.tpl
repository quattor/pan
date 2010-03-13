#
# different property (data) types can't be mixed, even via variables
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template compat3;

"/bug" = {
  x = 1;
  x = undef;
  x = "foo";
  y = 1;
  y = "foo";
  list(x, y);
};
