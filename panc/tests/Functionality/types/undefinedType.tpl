#
# the bind statement must verify that the referenced
# type has all subtypes already defined
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#


object template undefinedType;

# link is NOT a pre-defined type
bind "/x" = link;

"/x" = "/some/path";
