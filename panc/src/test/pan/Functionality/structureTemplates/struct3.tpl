#
# function statements are not allowed in structure templates
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
structure template struct3;

function f = 'BAD';

