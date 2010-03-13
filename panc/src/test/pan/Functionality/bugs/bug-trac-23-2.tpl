#
# Records must not allow invalid paths.
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
declaration template bug-trac-23-2;

type x = {
  'illegal/multilevel' ? string
};

