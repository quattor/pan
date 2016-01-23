#
# @expect="/nlist[@name='profile']/nlist[@name='result']/boolean[@name='a']='true' and /nlist[@name='profile']/nlist[@name='result']/long[@name='b']=10"
# @format=pan
#
object template default15;

type t = {
  'a' : boolean = true
  'b' : long = 10
  'c' ? double
} = nlist();

bind '/result' = t;
