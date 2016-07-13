#
# @expect=org.quattor.pan.exceptions.ValidationException ".*value \(ddd\) is not a possible choice value.*"
#

object template choice20;

type mychoice = choice("aa", "bb", "aaa", "bbb");
bind '/x' = mychoice with length(SELF) == 3;
'/x' = "ddd";
