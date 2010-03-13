#
# test of delete as statement
#
# @expect="count(/profile/list)=2 and /profile/list[1]='aa' and /profile/list[2]='dd' and count(/profile/nlist/*)=2 and /profile/nlist/xx=1 and /profile/nlist/zz=3"
#

object template delete1;

"/list" = list("aa", "bb", "cc", "dd");

"/nlist/xx" = 1;
"/nlist/yy" = 2;
"/nlist/zz" = 3;

"/list/1" = null;
"/list/1" = null;

"/nlist/yy" = null;
"/nlist/yy" = null;
