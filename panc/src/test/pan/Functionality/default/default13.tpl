#
# @expect="/profile/result[1]='true' and /profile/result[2]='true'"
#
object template default13;

type t = boolean[] = list(true,true);

bind '/result' = t;

