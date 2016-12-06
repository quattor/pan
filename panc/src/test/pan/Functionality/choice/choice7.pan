# Test ListType with ChoiceType
# @expect="/nlist[@name='profile']/list[@name='x']/*[1]='a' and /nlist[@name='profile']/list[@name='x']/*[2]='c' and /nlist[@name='profile']/list[@name='x']/*[3]='c'"
#

object template choice7;

type mychoice = choice("a", "b", "c")[];
bind '/x' = mychoice;
'/x' = list("a", "c", "c");
