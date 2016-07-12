#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template choice4;

type mychoice = choice("a", "b", "c");
bind '/x' = mychoice;

'/x' = "d";