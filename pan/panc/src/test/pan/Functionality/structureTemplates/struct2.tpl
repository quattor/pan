#
# variable statements are not allowed in structure templates
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
structure template struct2;

variable X = 'BAD';

