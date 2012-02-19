#
# Check that loop goes as expected.  (10 iterations) 
#
# @expect="/nlist[@name='profile']/long[@name='result']=10"
# @format=pan
#
object template forloop2;

'/result' = {
  x = list();
  for (i = 0; i < 10; i = i + 1) {
    x[length(x)] = i;
  };
  length(x);
};
