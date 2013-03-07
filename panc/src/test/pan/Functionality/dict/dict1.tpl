#
# test of dict creation
#
# @expect="/nlist[@name='profile']/nlist[@name='result']/string[@name='l1']='first'"
# @format=pan
#

object template dict1;

'/result' = dict('l1', 'first');
