#
# @expect="/nlist[@name='profile']/string[@name='t']='Hey'"
#

object template validate13;

type mystring = string with match(SELF, 'a$');

type x = string with {
  if (validate(mystring, SELF)) {
    return(false);
  };
  true;
};

bind '/t' = x;
'/t' = "Hey";
