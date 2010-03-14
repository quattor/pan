#
# @expect="/profile/result/alpha/a='true' and /profile/result/alpha/b=1.2 and /profile/result/beta/a='true' and /profile/result/beta/b=3.4"
#
object template default18;

type t = extensible {
  'a' : boolean = true
};

bind '/result' = t{};

'/result/alpha/b' = 1.2;
'/result/beta/b' = 3.4;
