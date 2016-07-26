#
# @expect="/nlist[@name='profile']/boolean[@name='res']='false'"
#

object template validate9;

type mystring = string(2..);

'/res' = validate(mystring, "m");
