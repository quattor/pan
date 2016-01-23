#
# Records must not allow invalid paths.
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
declaration template bug-trac-23-1;

type x = {
  '+++not+++valid+++path' ? string
};

