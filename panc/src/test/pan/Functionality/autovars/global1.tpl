#
# check that self is properly defined for variable definitions
#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template global1;

variable global = "OK";

variable global = {
  v = "NOT DEFINED";
  if (exists(SELF)) v = SELF;
  return(v);
};

"/result" = global;
