#
# @expect="/nlist[@name='profile']/nlist[@name='result']/string[@name='a']='OK'"
# @format=pan
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
