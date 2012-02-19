object template bug10640;
"/re"=1.2;

# @expect="/nlist[@name='profile']"
# @format=pan
#
# This fails on solaris with the optimizer turned on for the lexer.
# It is a segmentation fault in the new_double_constant_node function.
# It does not happen when the optimizer is turned off.  Strangely,
# if the path name is longer than three characters, the segmentation
# fault doesn't happen.
