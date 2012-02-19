# 
# Ensure that indirect changes to self are immediately 
# visible in the configuration tree.
#
# @expect="/nlist[@name='profile']/nlist[@name='result1']/string[@name='a']='OK' and /nlist[@name='profile']/nlist[@name='result1']/string[@name='b']='OK' and /nlist[@name='profile']/list[@name='result2']/*[1]='OK' and /nlist[@name='profile']/list[@name='result2']/*[2]='OK'"
# @format=pan
#
object template self6;

'/result1' = {
  SELF['a'] = 'OK';
  SELF['b'] = value('/result1/a');
  SELF;
};

'/result2' = {
  SELF[0] = 'OK';
  SELF[1] = value('/result2/0');
  SELF;
};
