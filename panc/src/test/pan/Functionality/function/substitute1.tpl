#
# @expect="/nlist[@name='profile']/string[@name='result']='ok1-ok2'"
# @format=pan
#
object template substitute1;

'/result' = substitute('${v1}-${v2}', dict('v1', 'ok1', 'v2', 'ok2'));

