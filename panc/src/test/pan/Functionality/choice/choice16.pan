# Test LinkType with ChoiceType
# @expect="/nlist[@name='profile']/string[@name='x']='/y' and /nlist[@name='profile']/string[@name='y']='a'"
#

object template choice16;

type mychoice = choice("a", "b", "c")*;
bind '/x' = mychoice;
'/y' = "a";
'/x' = '/y';
