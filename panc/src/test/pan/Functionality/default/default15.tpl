#
# @expect="/profile/result/a='true' and /profile/result/b=10"
#
object template default15;

type t = {
  'a' : boolean = true
  'b' : long = 10
  'c' ? double
} = nlist();

bind '/result' = t;
