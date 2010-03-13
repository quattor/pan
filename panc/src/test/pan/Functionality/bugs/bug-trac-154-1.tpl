#
# @expect="/profile/result/a='OK'"
#

object template bug-trac-154-1;

function func = {
  SELF['a'] = 'OK';
  SELF;
};

'/result' = undef;

'/result' = {
  func();
  SELF;
};
