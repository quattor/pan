#
# @expect="/nlist[@name='profile']/boolean[@name='res']='false'"
#

object template validate2;

type mystring = string(2..);
variable X = "m";

'/res' = validate(mystring, X);
