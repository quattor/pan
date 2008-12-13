#
# test of the famous insert_after function
#

object template list5;

function insert_after = {
  if (argc != 3 || !is_string(argv[0]) || !is_string(argv[1]) ||
      !is_list(argv[2]))
    error("usage: insert_after(string, string, list)");
  idx = index(argv[1], argv[2]);
  if (idx < 0) {
    # not found, we insert at the end
    splice(argv[2], length(argv[2]), 0, list(argv[0]));
  } else {
    # found, we insert just after
    splice(argv[2], idx+1, 0, list(argv[0]));
  };
  return(argv[2]);
};

"/l1" = list("apmd", "atd", "crond", "inet", "syslog");
"/l2" = insert_after("identd", "crond", value("/l1"));
"/l3" = insert_after("identd", "cr0nd", value("/l1"));
