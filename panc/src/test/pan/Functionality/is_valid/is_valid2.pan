#
# @expect="/nlist[@name='profile']/boolean[@name='res']='false'"
#

object template is_valid2;

type mystring = string(2..);
variable X = "m";

'/res' = is_valid(mystring, X);
