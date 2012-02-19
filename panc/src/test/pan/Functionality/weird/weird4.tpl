#
# variables can be (ab)used to create hardlinks and even loops :-(
#
# @expect="/nlist[@name='profile']/list[@name='x4']/*[1]=1 and /nlist[@name='profile']/list[@name='x4']/*[2]/*[1]=2 and /nlist[@name='profile']/list[@name='x4']/*[2]/*[2]/*[1]=1 and /nlist[@name='profile']/list[@name='x4']/*[2]/*[2]/*[2]/*[1]=2"
# @format=pan
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
