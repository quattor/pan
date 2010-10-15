#
# make sure protected resources when optimizing don't
# get changed from one profile to another
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template prefix4;

prefix '/a';

'result' = 'ok';

prefix '';

'result' = 'bad';

