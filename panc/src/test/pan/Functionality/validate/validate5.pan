#
# @expect="/nlist[@name='profile']/boolean[@name='res']='true'"
#

object template validate5;

type mystring = string(2..);

'/res' = {
  x = "message";
  validate(mystring, x);
};
