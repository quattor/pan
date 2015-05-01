# 
# Ensure that indirect changes to self are immediately 
# visible in the configuration tree.
#
# @expect="/nlist[@name='profile']/nlist[@name='result1']/string[@name='a']='OK' and /nlist[@name='profile']/nlist[@name='result1']/string[@name='b']='OK' and /nlist[@name='profile']/list[@name='result2']/*[1]='OK' and /nlist[@name='profile']/list[@name='result2']/*[2]='OK'"
# @format=pan
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