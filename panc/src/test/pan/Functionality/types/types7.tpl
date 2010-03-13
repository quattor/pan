#
# test of included record types
#
# @expect="/profile/t2 and /profile/t3"
#

object template types7;

type r1 = {
  "a" : long
  "b" : string
};

type r2 = {
  include r1
  "c" : string
};

type r3 = {
  include r2
  "d" : long
};


bind "/t2" = r2;
bind "/t3" = r3;

"/t2" = nlist("a", 1, "b", "hello", "c", "world");
"/t3" = nlist("a", 1, "b", "hello", "c", "world", "d", 123);
