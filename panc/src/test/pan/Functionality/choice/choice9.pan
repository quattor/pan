#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template choice9;

type mychoice = choice("a", "b", "c")[];
bind '/x' = mychoice;
'/x' = "a";
