#
# dep: circular
# dep: indirect
#
object template circular;

function check_external = {
  is_long(value('indirect:/a'));
};

valid '/' = check_external();

'/a' = 1;

 