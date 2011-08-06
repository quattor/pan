#
# sample file to test checking of property types shifted
# to the end of processing (all type checking is performed
# and the end)
#
# @expect="count(/profile/*)=3"
#

object template types2;
bind "/x" = long;
"/x" = "foo";
"/x" = undef;
"/x" = 1;

type uint = long with SELF >= 0;
bind "/y" = uint;
"/y" = -1;
"/y" = 1;

"/z" = 1;
bind "/z" = string;
"/z" = undef;
"/z" = "foo";
