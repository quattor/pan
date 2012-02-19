#
# @expect="/profile/result/a='OK'"
# @format=xmldb
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
