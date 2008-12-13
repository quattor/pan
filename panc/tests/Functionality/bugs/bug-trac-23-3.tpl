#
# Records must not allow invalid paths.
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
declaration template bug-trac-23-3;

# Numbers are not allowed.
type x = {
  '0' ? string
};

