#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template default12;

type t = boolean = true;

bind '/result' = t;

