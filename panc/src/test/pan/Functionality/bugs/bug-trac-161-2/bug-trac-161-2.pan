#
# @expect="/nlist[@name='profile']/list[@name='result']/*[1]/string[@name='a']='OK'"
# @format=pan
#

object template bug-trac-161-2;

'/result' = {
  SELF[length(SELF)] = create('test1');
  SELF;
};