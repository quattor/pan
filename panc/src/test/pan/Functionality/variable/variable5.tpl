#
# test of global variables
#
# @expect="/nlist[@name='profile']/nlist[@name='global']/string[@name='object']='variable5'"
# @format=pan
#

object template variable5;

"/global/object" = OBJECT;
