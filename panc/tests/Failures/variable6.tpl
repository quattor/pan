#
# test of variable assignments (tricky stuff)
#

object template variable6;

# <var> = <dml>: change what <var> points to in variable table
"/t1" = {
  x = y = 0;          # x and y refer to the same element
  y = y + 1;          # y gets replaced and now points to a new element
  return(list(x, y)); # return: (0, 1)
};
# ==> works like in most programing languages

# <var>[expr] = <dml>: replace <var> child's value but keep same id
"/t2" = {
  x = y[0] = 0;          # x and y[0] refer to the same element
  y[0] = y[0] + 1;       # y[0] gets a new value but x still refers to it
  return(list(x, y[0])); # return: (1, 1)
};
# ==> behaves like if x is a pointer to y[0]

# an element can only have one parent, automagically copy/clone it
# if this is not the case
"/t3" = {
  x[0] = y[0] = 0;       # x[0] gets a copy of y[0]
  y[0] = y[0] + 1;       # y[0] gets a new value
  return(list(x[0], y[0])); # return: (0, 1)
};
# ==> works like in most programing languages
