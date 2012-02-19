#
# @expect="/nlist[@name='profile']/nlist[@name='result']/boolean[@name='a']='true' and /nlist[@name='profile']/nlist[@name='result']/boolean[@name='b']='true'"
# @format=pan
#
object template default14;

type t = boolean{} = nlist('a',true,'b',true);

bind '/result' = t;

