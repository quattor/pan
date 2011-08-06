#
# check that self is properly defined for variable definitions
#
# @expect="/profile/result='OK'"
#
object template global1;

variable global = "OK";

variable global = {
  v = "NOT DEFINED";
  if (exists(SELF)) v = SELF;
  return(v);
};

"/result" = global;
