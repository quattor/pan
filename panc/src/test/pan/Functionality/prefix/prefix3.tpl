#
# make sure protected resources when optimizing don't
# get changed from one profile to another
#
# @expect="/profile/a/result='ok' and /profile/b/result='ok'"
# @format=xmldb
#

object template prefix3;

prefix '/a';

'result' = 'ok';

prefix '/b';

'result' = 'ok';

