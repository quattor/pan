#
# single quoted strings are supported too
#
# @expect="/nlist[@name='profile']/string[@name='x1']='foo' and /nlist[@name='profile']/string[@name='x2']='f^o$o"b\a`r' and /nlist[@name='profile']/string[@name='x3']="foo'bar" and /nlist[@name='profile']/string[@name='x4']="foo''""
# @format=pan
#

object template strings9;

"/x1" = 'foo';
"/x2" = 'f^o$o"b\a`r';
"/x3" = 'foo''bar';
"/x4" = 'foo''''';
