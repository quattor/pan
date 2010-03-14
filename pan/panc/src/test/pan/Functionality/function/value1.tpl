#
# @expect="/profile/result='true'"
#
object template value1;

'/result' = {
  exists('/result') || error("value must be hooked into tree before evaluating DML block");
};

