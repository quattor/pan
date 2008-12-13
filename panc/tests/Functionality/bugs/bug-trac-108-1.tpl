#
# @expect=org.quattor.pan.exceptions.ValidationException
#
object template bug-trac-108-1;

bind '/result' = string*;

# This should give a validation error with a 
# complaint about a non-resource path ('/x/y' here).
'/x/y' = 'STR';
'/result' = '/x/y/z';

