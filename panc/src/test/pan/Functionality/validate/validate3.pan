#
# @expect="/nlist[@name='profile']/boolean[@name='res']='true'"
#

object template validate3;

type mydict = string(1..){};
'/x' = dict('entry1', 'value1', 'entry2', 'value2');

'/res' = validate(mydict, value('/x'));
