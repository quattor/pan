#
# @expect="/nlist[@name='profile']/string[@name='x']='a'"
#

object template choice1;

type mychoice = choice("a", "b", "c");

bind '/x' = mychoice;
'/x' = "a";
