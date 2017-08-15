#
# @expect=org.quattor.pan.exceptions.ValidationException
#
object template default27;

type g = {
  'a' ? string
  'b' ? string
};

bind '/gs' = g{};

'/gs/h' = undef;
