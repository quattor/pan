#
# @expect="/profile/result[1]='true' and /profile/result[2]='true'"
#
object template unique1;

variable INDEX_O = 0;
variable INDEX_M = 0;

include {'once'};
include {'once'};

include {'multi'};
include {'multi'};

'/result' = list(INDEX_O==1,INDEX_M==2);
