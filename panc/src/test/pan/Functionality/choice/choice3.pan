#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template choice3;

type mychoice = choice("a", "b", "c");
bind '/x' = mychoice;

'/x' = 3;
