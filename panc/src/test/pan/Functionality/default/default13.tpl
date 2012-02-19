#
# @expect="/profile/result[1]='true' and /profile/result[2]='true'"
# @format=xmldb
#
object template default13;

type t = boolean[] = list(true,true);

bind '/result' = t;

