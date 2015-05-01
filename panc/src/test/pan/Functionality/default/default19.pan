#
# @expect="/nlist[@name='profile']/string[@name='result']='/mylong' and /nlist[@name='profile']/long[@name='mylong']='10'"
# @format=pan
#
object template default19;

type t = long* = '/mylong';

bind '/result' = t;

'/mylong' = 10;
