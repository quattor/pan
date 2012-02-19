#
# variables can be (ab)used to create hardlinks and even loops :-(
#
# @expect="/profile/x4[1]=1 and /profile/x4[2]/x4[1]=2 and /profile/x4[2]/x4[2]/x4[1]=1 and /profile/x4[2]/x4[2]/x4[2]/x4[1]=2"
# @format=xmldb
#

object template weird4;

# same element hooked twice _at different levels_
# (this now works: v is automagically cloned so there is no loop!)
"/x4" = {
  v[0] = 1;
  v[1][0] = 2;
  v[1][1] = v;
  return(v);
};
