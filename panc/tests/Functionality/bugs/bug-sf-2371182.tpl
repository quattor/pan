# 
# This code creates a data structure with a circular dependency.
# When the pan compiler tries to protect() the nlist to put it
# into the configuration, the protect() function recurses 
# infinitely causing a StackOverflowException. 
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template bug-sf-2371182;

function f = {
  SELF['A'] = nlist('a', 'BAD');
  SELF;
};

'/x' = {
  SELF['A'] = f();
  SELF;
};

'/result' = 'OK';
