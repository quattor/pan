#
# @expect="/nlist[@name='profile']/nlist[@name='result']/string[@name='a']='OK'"
# @format=pan
#
object template default23;

type x = {
  'a' ? string = 'OK'
};

bind '/result' = x;

'/result/a' = undef;
