#
# test of list creation by index
#
# @expect="/profile/l1[1]='first' and /profile/l2[1]='first' and /profile/l3[1]='first' and /profile/l1[2]='second' and /profile/l2[2]='second' and /profile/l3[2]='second' and /profile/l1[3]='third' and /profile/l2[3]='third' and /profile/l3[3]='third'"
#

object template list1;

"/l1/0" = "first";
"/l1/1" = "second";
"/l1/2" = "third";

"/l2/2" = "third";
"/l2/1" = "second";
"/l2/0" = "first";

"/l3/2" = "third";
"/l3/0" = "first";
"/l3/1" = "second";
