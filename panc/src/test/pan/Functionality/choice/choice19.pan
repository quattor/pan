#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template choice19;

type mychoice = choice("aa", "bb", "aaa", "bbb");
bind '/x' = mychoice with length(SELF) == 3;
'/x' = "aa";