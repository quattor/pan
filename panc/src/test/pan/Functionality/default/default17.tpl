#
# @expect="/profile/result[1]/a='true' and /profile/result[1]/b=1.2 and /profile/result[2]/a='true' and /profile/result[2]/b=3.4"
#
object template default17;

type t = extensible {
  'a' : boolean = true
};

bind '/result' = t[];

'/result/0/b' = 1.2;
'/result/1/b' = 3.4;
