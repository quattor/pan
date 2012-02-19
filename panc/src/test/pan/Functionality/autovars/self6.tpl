# 
# Ensure that indirect changes to self are immediately 
# visible in the configuration tree.
#
# @expect="/profile/result1/a='OK' and /profile/result1/b='OK' and /profile/result2[1]='OK' and /profile/result2[2]='OK'"
# @format=xmldb
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
