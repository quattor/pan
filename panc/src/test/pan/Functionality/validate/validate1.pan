#
# @expect="/nlist[@name='profile']/boolean[@name='res']='true'"
#

object template validate1;

type mystring = string(2..);
variable X = "message";

'/res' = validate(mystring, X);
