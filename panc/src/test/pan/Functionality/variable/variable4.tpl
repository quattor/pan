#
# complicated nested variable lookups
#
# @expect="/nlist[@name='profile']/list[@name='x1']/*[1]/*[1]=0 and /nlist[@name='profile']/list[@name='x1']/*[2]/*[1]=1 and /nlist[@name='profile']/list[@name='x1']/*[3]/*[1]=3 and /nlist[@name='profile']/list[@name='x1']/*[3]/*[2]=2"
# @format=pan
#

object template variable4;

"/x1" = {
 a[0] = 0;
 b[0] = 2;
 c[0] = 3;
 b[a[0]] = 1;
 c[b[0]] = 2;
 list(a, b, c);
};
