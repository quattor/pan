#
# @expect="/nlist[@name='profile']/boolean[@name='res']='false'"
#

object template is_valid9;

type mystring = string(2..);

'/res' = is_valid(mystring, "m");
