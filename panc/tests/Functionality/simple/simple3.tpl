#
# test of self in assignments
#
# @expect="/profile/test1=1 and /profile/test2=2 and /profile/test3[1]=1 and /profile/test3[2]=2 and /profile/test4[1]=1 and /profile/test4[2]='a' and /profile/test4[3]='b' and /profile/test4[4]=2"
#

object template simple3;

"/test1" = 1;
"/test1" = SELF;

"/test2" = 1;
"/test2" = SELF + 1;

"/test3" = list(1,2);
"/test3" = SELF;

"/test4" = list(1,2);
"/test4" = splice(SELF, 1, 0, list("a", "b"));
