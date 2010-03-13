#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template bug-trac-161-5;

function f = {
  SELF['y'] = 'BAD';
  true;
};

bind '/result' = string{} with f();
'/result/x' = 'OK';
