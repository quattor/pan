#
# @expect="/profile/result='a-b-c-d'"
#
object template replace1;

'/result' = replace('\d', '-', 'a1b2c3d');

