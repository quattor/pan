#
# @expect="/profile/result='/mylong' and /profile/mylong=10"
#
object template default19;

type t = long* = '/mylong';

bind '/result' = t;

'/mylong' = 10;
