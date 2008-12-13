#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
object template bug-trac-175-2;
'/result' = match(undef, 'x');
