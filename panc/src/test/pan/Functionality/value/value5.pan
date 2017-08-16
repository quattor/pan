# @expect="/nlist[@name='profile']/string[@name='self']='myself'"
# @format=pan
#
object template value5;

"/self" = "myself";
"/self" = value("/nopath", SELF);
