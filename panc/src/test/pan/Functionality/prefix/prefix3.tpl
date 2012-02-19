#
# make sure protected resources when optimizing don't
# get changed from one profile to another
#
# @expect="/nlist[@name='profile']/nlist[@name='a']/string[@name='result']='ok' and /nlist[@name='profile']/nlist[@name='b']/string[@name='result']='ok'"
# @format=pan
#

object template prefix3;

prefix '/a';

'result' = 'ok';

prefix '/b';

'result' = 'ok';

