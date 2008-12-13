#
# bind statements are not allowed in structure templates
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
structure template struct1;

bind '/x' = boolean;

