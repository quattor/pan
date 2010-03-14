#
# dep: validation
# dep: indirect
#
object template validation;

function check_external = {
  is_long(value('indirect:/a'));
};

valid '/' = check_external();

 