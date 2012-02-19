# 
# Multi-level assignment to non-existant SELF should
# create all necessary resources.
#
# @expect="/nlist[@name='profile']/list[@name='result']/*[1]/*[1]='OK'"
# @format=pan
#
object template self4;

'/result' = {
  SELF[0][0] = 'OK';
  SELF;
};
