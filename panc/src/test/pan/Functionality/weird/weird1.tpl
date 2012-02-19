#
# variables can be (ab)used to create hardlinks and even loops :-(
#
# @expect="/profile/x1[1]='OK' and /profile/x1[2]='OK'"
# @format=xmldb
#

object template weird1;

# like list() but does not clone the result
# (it is now forbidden to access argv directly)
function list_no_clone1 = ARGV;

# same element hooked twice _at same level_
"/x1" = {
  x = "OK";
  return(list_no_clone1(x, x));
};
