# 
# Ensure that changes of the SELF variable is visible
# in different stack frames.
#
# @expect="/nlist[@name='profile']/nlist[@name='result']/string[@name='a']='OK' and /nlist[@name='profile']/nlist[@name='result']/string[@name='b']='OK'"
# @format=pan
#
object template self12;

function f = {
  SELF['a'];
};

'/result' = {
  SELF['a'] = 'OK';
  SELF['b'] = f();
  SELF;
};
