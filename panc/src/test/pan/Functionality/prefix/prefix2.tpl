#
# make sure protected resources when optimizing don't
# get changed from one profile to another
#
# @expect="/nlist[@name='profile']/string[@name='result']='ok'"
# @format=pan
#

object template prefix2;

prefix '/a';

'/result' = 'ok';
