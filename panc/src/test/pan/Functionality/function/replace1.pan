#
# @expect="/nlist[@name='profile']/string[@name='result']='a-b-c-d'"
# @format=pan
#
object template replace1;

'/result' = replace('\d', '-', 'a1b2c3d');

