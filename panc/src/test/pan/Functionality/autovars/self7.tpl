# 
# Ensure that indirect changes to self are immediately 
# visible in the configuration tree.
#
# @expect="/profile/result1/a='OK' and /profile/result1/b='OK' and /profile/result2[1]='OK' and /profile/result2[2]='OK'"
#
object template self7;

variable T1 = {
  SELF['a'] = 'OK';
  SELF['b'] = T1['a'];
  SELF;
};

variable T2 = {
  SELF[0] = 'OK';
  SELF[1] = T2[0];
  SELF;
};

'/result1' = T1;
'/result2' = T2;