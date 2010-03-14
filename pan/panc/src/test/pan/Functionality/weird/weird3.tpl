#
# variables can be (ab)used to create hardlinks and even loops :-(
#
# @expect="/profile/x3[1]='OK' and /profile/x3[2]='OK'"
#

object template weird3;

# same element hooked twice _at same level_
# (this now works because the variable is automagically cloned)
"/x3" = {
  x = "OK";
  v[0] = x;
  v[1] = x;
  return(v);
};
