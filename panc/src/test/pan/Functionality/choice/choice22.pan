#
# @expect=org.quattor.pan.exceptions.ValidationException ".*string size \(2\) is outside range.*"
#

object template choice22;

type mychoice = choice(
    "aa",
    "bb",
    "aaa",
    "bbb",
);
bind '/x' = mychoice(3);
'/x' = "aa";
