#
# @expect="/nlist[@name='profile']/string[@name='result']='true-1'"
# @format=pan
#
object template substitute8;

variable V1 = true;

'/result' = {
  v2 = 1;
  substitute('${V1}-${v2}');
};

