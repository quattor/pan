#
# @expect="/nlist[@name='profile']/boolean[@name='res']='false'"
#

object template is_valid4;

type mydict = string(2..){};
'/x' = dict('entry1', 'm');

'/res' = is_valid(mydict, value('/x'));
