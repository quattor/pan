#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template choice11;

type mychoice = choice("a", "b", "c"){};
bind '/x' = mychoice;
'/x' = dict("entry1", "b", "entry2", "d");
