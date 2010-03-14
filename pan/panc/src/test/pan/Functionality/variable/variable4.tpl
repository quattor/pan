#
# complicated nested variable lookups
#
# @expect="/profile/x1[1]/x1[1]=0 and /profile/x1[2]/x1[1]=1 and /profile/x1[3]/x1[1]=3 and /profile/x1[3]/x1[2]=2"
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
