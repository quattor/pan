# Test HashType with ChoiceType
# @expect="/nlist[@name='profile']/nlist[@name='x']/string[@name='entry1']='b' and /nlist[@name='profile']/nlist[@name='x']/string[@name='entry2']='c'"
#

object template choice9;

type mychoice = choice("a", "b", "c"){};
bind '/x' = mychoice;
'/x' = dict("entry1", "b", "entry2", "c");