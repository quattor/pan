#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template exists9;

# Check that using exists on the same variable that is being
# set always returns true.  This is a change in behavior 
# between v8 and v7 of the compiler.

# In particular, this returned false for v7 and returns true
# for v8.
variable X = exists(X);

variable Y = undef;
variable Y = exists(Y);

variable Z = null;
variable Z = exists(Z);

'/result' = X && Y && Z;
