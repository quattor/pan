#
# @expect="/profile/result/a/b='OK'"
#

# 
# Modifications to copies of global variables must not 
# affect the global variable itself.
#
object template delete11;

variable X = nlist('a', nlist('b','OK','c','OK'));

variable Y = {
  copy = X;
  copy['a']['b'] = null;
  'OK';
};


'/result' = X;