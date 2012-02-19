#
# @expect="/profile/result/a='OK'"
# @format=xmldb
#

object template bug-trac-154-2;

function func = {
  SELF['a'] = 'OK';
  SELF;
};

'/result' = nlist();

'/result' = {
  func();
  SELF;
};
