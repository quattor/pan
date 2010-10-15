#
# make sure protected resources when optimizing don't
# get changed from one profile to another
#
# @expect="/profile/result='ok'"
#

object template prefix2;

prefix '/a';

'/result' = 'ok';
