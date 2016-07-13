#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template choice14;

type mychoice = {
    'entry1' : string
    'entry2' : choice("a", "b")
};

bind '/x' = mychoice;
'/x' = dict("entry1", "ok", "entry2", "d");
