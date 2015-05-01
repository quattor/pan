#
# see if globals can be modified indirectly via iterators in dml
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template global2;

variable global = "OK";

# This code implicitly creates a local variable called
# global which masks the real "global" variable.  This cannot be
# done explicitly.  If this is the desired behavior this should
# be documented in the pan specification. 
"/result" = {
  value(global); # This prevents this block from being optimized.
  hash = nlist("one","BAD");
  ok = first(hash,k,global);
  return(global);
};

"/data" = global;
