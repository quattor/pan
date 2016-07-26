#
# @expect="/nlist[@name='profile']/boolean[@name='res']='false'"
#

object template validate6;

type mystring = string(2..);

'/res' = {
  x = "m";
  validate(mystring, x);
};
