#
# debug arguments are not processed when not debugging
# (so you can safely use it without performance penalty)
#
#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#

object template function10;

function function10_test = {
  error("function10_test is called");
};

"/result" = {
  debug(function10_test());
  "OK";
};
