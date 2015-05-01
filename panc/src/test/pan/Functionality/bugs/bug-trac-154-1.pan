#
# @expect="/nlist[@name='profile']/nlist[@name='result']/string[@name='a']='OK'"
# @format=pan
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
