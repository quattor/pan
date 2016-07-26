#
# @expect="/nlist[@name='profile']/boolean[@name='res']='true'"
#

object template validate8;

type mystring = string(2..);

'/res' = validate(mystring, "message");
