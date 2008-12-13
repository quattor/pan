#
# failed type validation
#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template validation4;

# strings of 3 characters
type str3 = string(3);

# table of these
bind "/x" = str3{};

"/x/a" = "foo";
"/x/b" = "oops";
"/x/c" = "bar";

