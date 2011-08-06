# 
# Ensure that SELF cannot be modified from validation 
# functions. 
#
# @expect=org.quattor.pan.exceptions.ValidationException
#
object template self8;

valid '/result' = {
  SELF['a'] = 'BAD';
  SELF;
};
