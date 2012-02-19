#
# @expect="/nlist[@name='profile']/string[@name='a']='one' and /nlist[@name='profile']/string[@name='b']='two'"
# @format=pan
#
object template default21;

type g = {
  'a' : string = '1'
  'b' : string = '2'
};

bind '/' = g;

'/a' = 'one';
'/b' = 'two';

