#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template choice12;

type mychoice = choice("a", "b", "c"){};
bind '/x' = mychoice;
'/x' = list("a", "b", "b", "c");