object template indirect;

function check_external = {
  is_long(value('circular:/a'));
};

valid '/' = check_external();

'/a' = 1;

