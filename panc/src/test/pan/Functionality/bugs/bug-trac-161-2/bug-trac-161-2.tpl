#
# @expect="/profile/result[1]/a='OK'"
#

object template bug-trac-161-2;

'/result' = {
  SELF[length(SELF)] = create('test1');
  SELF;
};