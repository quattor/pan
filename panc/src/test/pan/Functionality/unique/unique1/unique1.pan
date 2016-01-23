#
# @expect="/nlist[@name='profile']/list[@name='result']/*[1]='true' and /nlist[@name='profile']/list[@name='result']/*[2]='true'"
# @format=pan
#
object template unique1;

variable INDEX_O = 0;
variable INDEX_M = 0;

include {'once'};
include {'once'};

include {'multi'};
include {'multi'};

'/result' = list(INDEX_O==1,INDEX_M==2);
