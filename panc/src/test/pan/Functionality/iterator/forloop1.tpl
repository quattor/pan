#
# Check that loop with no iterations returns the
# initialization value. 
#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template forloop1;

'/result' = {
  for ('OK'; false; undef) {
    undef;
  };
};
