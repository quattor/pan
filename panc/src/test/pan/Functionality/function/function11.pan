#
# test of to_long function
#
# @expect="/nlist[@name='profile']/long[@name='l1']=291 and /nlist[@name='profile']/long[@name='l2']=291 and /nlist[@name='profile']/long[@name='l3']=46 and /nlist[@name='profile']/long[@name='l4']=1"
# @format=pan
#

object template function11;

"/l1" = to_long("0x123");
"/l2" = to_long(0x123);
"/l3" = to_long(4.56e+1);
"/l4" = to_long(true);
