#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template unique2;

include {'tpl_ordinary'};
include {'tpl_unique'};
include {'tpl_declaration'};

'/result' = true;
