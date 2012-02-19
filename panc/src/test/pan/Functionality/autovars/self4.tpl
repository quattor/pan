# 
# Multi-level assignment to non-existant SELF should
# create all necessary resources.
#
# @expect="/profile/result[1]/result[1]='OK'"
# @format=xmldb
#
object template self4;

'/result' = {
  SELF[0][0] = 'OK';
  SELF;
};
