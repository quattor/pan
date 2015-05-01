#
# test to_double function
#
# @expect="/nlist[@name='profile']/double[@name='d1']=45.6 and /nlist[@name='profile']/double[@name='d2']=123.0 and /nlist[@name='profile']/double[@name='d3']=45.6 and /nlist[@name='profile']/double[@name='d4']=1.0"
# @format=pan
#

object template function12;

"/d1" = to_double("4.56e+1");
"/d2" = to_double(123);
"/d3" = to_double(4.56e+1);
"/d4" = to_double(true);
