#
# single quoted strings are supported too
#
# @expect="/profile/x1='foo' and /profile/x2='f^o$o"b\a`r' and /profile/x3="foo'bar" and /profile/x4="foo''""
#

object template strings9;

"/x1" = 'foo';
"/x2" = 'f^o$o"b\a`r';
"/x3" = 'foo''bar';
"/x4" = 'foo''''';
