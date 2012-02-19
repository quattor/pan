#
# test yet more builtin functions
#
# @expect="/profile/s1='cdef' and /profile/s2='b' and /profile/s3='bcde' and /profile/s4='cdef' and /profile/s5='c' and /profile/s6='cde'"
# @format=xmldb
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

