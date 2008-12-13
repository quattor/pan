#
# test of included record types
#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template types8;

type r1 = {
  "a" : long
  "b" : string
};

type r2 = {
  include r1
  "c" : string
};

bind "/t1" = r2;

"/t1" = nlist("b", "hello", "c", "world");
