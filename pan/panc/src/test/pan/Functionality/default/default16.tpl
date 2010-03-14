#
# @expect="/profile/result/a='true' and /profile/result/b=10"
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
