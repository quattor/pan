# Test for default value should fail
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template choice6;

type mychoice = choice("a", "b", "c") = "d";
bind '/x' = mychoice;