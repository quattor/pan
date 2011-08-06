#
# SELF cannot be the direct target of an assignment. 
# This error is caught at compilation time.
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
object template self3;

'/result' = {
  SELF = 'a';
};
