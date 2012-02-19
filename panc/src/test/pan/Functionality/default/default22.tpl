#
# @expect="/nlist[@name='profile']/string[@name='a']=1 and /nlist[@name='profile']/string[@name='b']=2"
# @format=pan
#
object template default22;

type g = {
  'a' : string = '1'
  'b' : string = '2'
};

bind '/' = g;
