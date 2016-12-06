# Test default value for choice type
# @expect="/nlist[@name='profile']/string[@name='x']='a'"
#

object template choice5;

type mychoice = choice("a", "b", "c") = "a";
bind '/x' = mychoice;
