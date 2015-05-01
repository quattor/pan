# 
# Ensure that changes of the SELF variable in different
# frames are properly propagated to other frames.
#
# @expect="/nlist[@name='profile']/nlist[@name='result']/string[@name='a']='OK'"
# @format=pan
#
object template self11;

function f = {
  SELF['a'] = 'OK';
  'dummy';
};

'/result' = {
  f();
  SELF;
};
