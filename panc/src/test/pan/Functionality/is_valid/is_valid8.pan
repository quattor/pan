#
# @expect="/nlist[@name='profile']/boolean[@name='res']='true'"
#

object template is_valid8;

type mystring = string(2..);

'/res' = is_valid(mystring, "message");
