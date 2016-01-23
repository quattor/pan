#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template value1;

'/result' = {
  exists('/result') || error("value must be hooked into tree before evaluating DML block");
};

