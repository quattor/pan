#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template choice8;

type mychoice = choice("a", "b", "c")[];
bind '/x' = mychoice;
'/x' = list("a", "c", "d");
