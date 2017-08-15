#
# @expect="/nlist[@name='profile']/nlist[@name='h']/nlist[@name='a']/long[@name='y']=10"
# @format=pan
#
object template default28;

type f = {
    'x' ? long
    'y' ? long
} = dict('y', 10);

type g = {
    'a' ? f
    'b' ? string
} = dict('a', undef);

bind '/h' = g;
'/h' = undef;
