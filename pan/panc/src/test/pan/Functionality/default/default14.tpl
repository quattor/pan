#
# @expect="/profile/result/a='true' and /profile/result/b='true'"
#
object template default14;

type t = boolean{} = nlist('a',true,'b',true);

bind '/result' = t;

