#
# Check that loop with no iterations returns the
# initialization value. 
#
# @expect="/profile/result='OK'"
#
object template forloop1;

'/result' = {
  for ('OK'; false; undef) {
    undef;
  };
};
