#
# @expect="/nlist[@name='profile']/boolean[@name='res']='false'"
#

object template validate4;

type mydict = string(2..){};
'/x' = dict('entry1', 'm');

'/res' = validate(mydict, value('/x'));
