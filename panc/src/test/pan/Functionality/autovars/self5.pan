# 
# Multi-level assignment to non-existant SELF should
# create all necessary resources.
#
# @expect="/nlist[@name='profile']/nlist[@name='result']/nlist[@name='a']/string[@name='b']='OK'"
# @format=pan
#
object template self5;

'/result' = {
  SELF['a']['b'] = 'OK';
  SELF;
};
