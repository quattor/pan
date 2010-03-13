#
# @expect="/profile/result='true'"
#
object template unique2;

include {'tpl_ordinary'};
include {'tpl_unique'};
include {'tpl_declaration'};

'/result' = true;
