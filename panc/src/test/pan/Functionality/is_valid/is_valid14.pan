#
# @expect=org.quattor.pan.exceptions.ValidationException ".*user-defined validation failed.*"
#

object template is_valid14;

type mystring = string with match(SELF, 'a$');

type x = string with {
  if (is_valid(mystring, SELF)) {
    return(false);
  };
  true;
};

bind '/t' = x;
'/t' = "Heya";
