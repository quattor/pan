#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template exists8;

# Need to be sure that exists() works correctly for external
# paths with new form.

'/result' = exists('obj1:/x');

