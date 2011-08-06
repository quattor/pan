#
# global variable cannot be modified from DML
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template terms1;

'/result' = {
  a[1.0] = 1;
};

