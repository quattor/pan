#
# tests of functions modifying their arguments
#

object template function9;

# the following function will increment its first arg using the second one
function function9_increment = {
  argv[0] = argv[0] + argv[1];
};

# the following function will add an element at the end of a list
# (cool compact stuff with no error checking at all)
function function9_push = {
  argv[0][length(argv[0])] = argv[1];
};

# we modify a variable
"/t1" = {
  x = 1;
  function9_increment(x, 2);
  return(x);
};

# we modify a constant!
# (this works too as function9_increment returns in fact the result)
"/t2" = {
  function9_increment(1, 2);
};

"/t3" = {
  x = list("a", "b");
  function9_push(x, "c");
  return(x);
};
