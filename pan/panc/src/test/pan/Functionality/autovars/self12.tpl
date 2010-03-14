# 
# Ensure that changes of the SELF variable is visible
# in different stack frames.
#
# @expect="/profile/result/a='OK' and /profile/result/b='OK'"
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
