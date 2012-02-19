#
# @expect="/nlist[@name='profile']/list[@name='result']/*[1]='true' and /nlist[@name='profile']/list[@name='result']/*[2]='true'"
# @format=pan
#
object template default13;

type t = boolean[] = list(true,true);

bind '/result' = t;

