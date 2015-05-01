#
# global variable cannot be modified from DML
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template terms2;

'/result' = {
  a['invalid/term'] = 1;
};

