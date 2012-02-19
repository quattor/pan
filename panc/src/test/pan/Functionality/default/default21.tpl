#
# @expect="/profile/a='one' and /profile/b='two'"
# @format=xmldb
#
object template default21;

type g = {
  'a' : string = '1'
  'b' : string = '2'
};

bind '/' = g;

'/a' = 'one';
'/b' = 'two';

