#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
object template file_contents2;

type t = {
  'a' : string = file_contents('some/path.xml')
};
