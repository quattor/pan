#
# test of delete as statement
#
# @expect="count(/nlist[@name='profile']/list[@name='list']/*)=2 and /nlist[@name='profile']/list[@name='list']/*[1]='aa' and /nlist[@name='profile']/list[@name='list']/*[2]='dd' and count(/nlist[@name='profile']/nlist[@name='nlist']/*)=2 and /nlist[@name='profile']/nlist[@name='nlist']/long[@name='xx']=1 and /nlist[@name='profile']/nlist[@name='nlist']/long[@name='zz']=3"
# @format=pan
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
