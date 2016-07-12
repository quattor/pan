#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template choice17;

type mychoice = choice("a", "b", "c")*;
bind '/x' = mychoice;
'/y' = "d";
'/x' = '/y';