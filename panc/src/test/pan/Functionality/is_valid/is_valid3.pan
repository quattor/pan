#
# @expect="/nlist[@name='profile']/boolean[@name='res']='true'"
#

object template is_valid3;

type mydict = string(1..){};
'/x' = dict('entry1', 'value1', 'entry2', 'value2');

'/res' = is_valid(mydict, value('/x'));
