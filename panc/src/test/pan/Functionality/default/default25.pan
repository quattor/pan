#
# @expect="/nlist[@name='profile']/nlist[@name='h']/nlist[@name='a']/long[@name='y']=10 and /nlist[@name='profile']/nlist[@name='i']/string[@name='b']='bb'"
# @format=pan
#
object template default25;

type f = {
    'x' ? long
    'y' ? long
} = dict('y', 10);

type g = {
  'a' ? f
  'b' ? string
} = dict('a', undef);

bind '/' = g{};

'/h' = undef;
'/i/b' = 'bb';
