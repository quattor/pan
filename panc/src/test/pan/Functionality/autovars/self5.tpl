# 
# Multi-level assignment to non-existant SELF should
# create all necessary resources.
#
# @expect="/profile/result/a/b='OK'"
# @format=xmldb
#
object template self5;

'/result' = {
  SELF['a']['b'] = 'OK';
  SELF;
};
