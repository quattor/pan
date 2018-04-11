#
# @expect="/nlist[@name='profile']/list[@name='h']/nlist[1]/string[@name='b']='bb' and /nlist[@name='profile']/list[@name='h']/nlist[2]/nlist[@name='a']/long[@name='y']=10"
# @format=pan
#
object template default26;

type f = {
    'x' ? long
    'y' ? long
} = dict('y', 10);

type g = {
  'a' ? f
  'b' ? string
} = dict('a', undef);

bind '/h' = g[];

'/h/0/b' = 'bb';
'/h/1' = undef;
