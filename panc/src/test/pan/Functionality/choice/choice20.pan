#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template choice20;

type mychoice = choice("aa", "bb", "aaa", "bbb");
bind '/x' = mychoice with length(SELF) == 3;
'/x' = "ddd";