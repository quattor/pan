# 
# Ensure that changes of the SELF variable in different
# frames are properly propagated to other frames.
#
# @expect="/profile/result/a='OK'"
# @format=xmldb
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
