#
# test yet more builtin functions
#
# @expect="/nlist[@name='profile']/string[@name='s1']='cdef' and /nlist[@name='profile']/string[@name='s2']='b' and /nlist[@name='profile']/string[@name='s3']='bcde' and /nlist[@name='profile']/string[@name='s4']='cdef' and /nlist[@name='profile']/string[@name='s5']='c' and /nlist[@name='profile']/string[@name='s6']='cde'"
# @format=pan
#

object template function4;

#
# substr() behaves like Perl's homonym
#
"/s1" = substr("abcdef", 2);
"/s2" = substr("abcdef", 1, 1);
"/s3" = substr("abcdef", 1, -1);
"/s4" = substr("abcdef", -4);
"/s5" = substr("abcdef", -4, 1);
"/s6" = substr("abcdef", -4, -1);

