#
# @expect="/nlist[@name='profile']/boolean[@name='a']='true' and /nlist[@name='profile']/boolean[@name='b']='true'"
# @format=pan
#
object template variable16;

variable A = null;
variable B = A;
"/a" = is_null(A);
"/b" = is_null(B);
