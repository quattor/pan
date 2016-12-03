#
# @expect="/nlist[@name='profile']/boolean[@name='res']='true'"
#

object template is_valid1;

type mystring = string(2..);
variable X = "message";

'/res' = is_valid(mystring, X);
