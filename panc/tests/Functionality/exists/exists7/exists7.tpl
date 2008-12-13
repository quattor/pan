#
# @expect="/profile/result='true'"
#
object template exists7;

# Need to be sure that exists() works correctly for external
# paths with both the new and old form.

'/result' = exists('//obj1/x');

