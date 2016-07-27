#
# @expect="/nlist[@name='profile']/string[@name='t']='Hey'"
#

object template is_valid13;

type mystring = string with match(SELF, 'a$');

type x = string with {
  if (is_valid(mystring, SELF)) {
    return(false);
  };
  true;
};

bind '/t' = x;
'/t' = "Hey";
