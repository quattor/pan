#
# @expect="/nlist[@name='profile']/string[@name='x']='aaa'"
#

object template choice21;

type mychoice = choice("aa", "bb", "aaa", "bbb");
bind '/x' = mychoice(3);
'/x' = "aaa";