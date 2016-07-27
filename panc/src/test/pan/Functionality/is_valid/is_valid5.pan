#
# @expect="/nlist[@name='profile']/boolean[@name='res']='true'"
#

object template is_valid5;

type mystring = string(2..);

'/res' = {
  x = "message";
  is_valid(mystring, x);
};
