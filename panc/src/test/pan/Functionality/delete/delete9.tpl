#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template delete9;

function set_null = {
  r = 10;
  r = null;
  return(null);
};

'/removed' = 1;
'/removed' = set_null();
'/result' = !exists('/removed');
