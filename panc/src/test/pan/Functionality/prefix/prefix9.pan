#
# Allow all relative paths with prefix
#
# @expect="/nlist[@name='profile']/list[@name='a']/*[1]='ok'"
# @format=pan
#

object template prefix9;

prefix '/a';

'0' = 'ok';
