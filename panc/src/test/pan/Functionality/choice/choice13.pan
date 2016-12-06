#
# @expect="/nlist[@name='profile']/nlist[@name='x']/string[@name='entry2']='a'"
#

object template choice13;

type mychoice = {
    'entry1' : string
    'entry2' : choice("a", "b")
};

bind '/x' = mychoice;
'/x' = dict("entry1", "ok", "entry2", "a");
