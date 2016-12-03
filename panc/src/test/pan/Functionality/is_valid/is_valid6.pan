#
# @expect="/nlist[@name='profile']/boolean[@name='res']='false'"
#

object template is_valid6;

type mystring = string(2..);

'/res' = {
  x = "m";
  is_valid(mystring, x);
};
