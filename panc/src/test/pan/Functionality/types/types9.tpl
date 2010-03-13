#
# record filed names can only be strings
# (needed to avoid clashes with reserved keywords)
#
# @expect=org.quattor.pan.parser.ParseException
#

object template types9;

type types9 = {
  foo    : long
  "type" : string
};
