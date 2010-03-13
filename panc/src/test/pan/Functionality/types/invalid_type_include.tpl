#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template invalid_type_include;

type a = boolean;

type b = {
  include a
  'one' : long
} = nlist();

bind '/result' = b;

