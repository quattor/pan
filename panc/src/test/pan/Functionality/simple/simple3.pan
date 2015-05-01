#
# test of self in assignments
#
# @expect="/nlist[@name='profile']/long[@name='test1']=1 and /nlist[@name='profile']/long[@name='test2']=2 and /nlist[@name='profile']/list[@name='test3']/*[1]=1 and /nlist[@name='profile']/list[@name='test3']/*[2]=2 and /nlist[@name='profile']/list[@name='test4']/*[1]=1 and /nlist[@name='profile']/list[@name='test4']/*[2]='a' and /nlist[@name='profile']/list[@name='test4']/*[3]='b' and /nlist[@name='profile']/list[@name='test4']/*[4]=2"
# @format=pan
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
