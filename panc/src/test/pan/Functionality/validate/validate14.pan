#
# @expect=org.quattor.pan.exceptions.ValidationException ".*user-defined validation failed.*"
#

object template validate14;

type mystring = string with match(SELF, 'a$');

type x = string with {
  if (validate(mystring, SELF)) {
    return(false);
  };
  true;
};

bind '/t' = x;
'/t' = "Heya";
