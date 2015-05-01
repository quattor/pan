#
# @expect="/nlist[@name='profile']/string[@name='result']='ok1-ok2'"
# @format=pan
#
object template substitute5;

variable V1 = 'ok1';

'/result' = {
  v2 = 'ok2';
  substitute('${V1}-${v2}');
};

