#
# @expect="/nlist[@name='profile']/nlist[@name='result']/boolean[@name='a']='true' and /nlist[@name='profile']/nlist[@name='result']/long[@name='b']=10"
# @format=pan
#
object template default16;

type t = {
  'a' : boolean = true
};

type s = {
  include t
  'b' : long = 10
} = nlist();

bind '/result' = s;
