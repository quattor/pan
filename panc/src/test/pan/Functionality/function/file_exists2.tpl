#
# @expect=org.quattor.pan.exceptions.SyntaxException
#
object template file_exists2;

type t = {
  'a' : string = file_exists('some/path.xml')
};
