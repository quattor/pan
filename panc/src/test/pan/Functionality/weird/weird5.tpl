#
# variables can be (ab)used to create hardlinks and even loops :-(
#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template weird5;

# simple loop
# (this now fails because the automagic clone leaves an undef in x)
"/x5" = {
  x = undef;
  y[0] = x;
  x[0] = y;
  return(x);
};
