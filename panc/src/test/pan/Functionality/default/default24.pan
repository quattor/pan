#
# @expect="/nlist[@name='profile']/boolean[@name='a']='true'"
# @format=pan
#
object template default24;

variable N = null;
variable N ?= 1;
"/a" = is_null(N);
