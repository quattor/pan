#
# variables can be (ab)used to create hardlinks and even loops :-(
#
# @expect="/profile/x2[1]/x2[1]='OK' and /profile/x2[2]='OK'"
#

object template weird2;

# like list() but does not clone the result
# (it is now forbidden to access argv directly)
function list_no_clone2 = ARGV;

# same element hooked twice _at different levels_
"/x2" = {
  x[0] = "OK";
  return(list_no_clone2(x, x[0]));
};
