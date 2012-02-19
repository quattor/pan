#
# regexp for IPv4 addresses
#
# @expect="/profile/checks[1]='BAD' and /profile/checks[2]='BAD' and /profile/checks[3]='BIG' and /profile/checks[4]='OK'"
# @format=xmldb
#

object template regexp3;

function check_ipv4 = {
  if (ARGC != 1)
    error("check_ipv4: takes one argument");
  if (!is_string(ARGV[0]))
    error("check_ipv4: takes one string argument");
  result = matches(ARGV[0], '^(\d+)\.(\d+)\.(\d+)\.(\d+)$');
  if (length(result) == 0)
    return("BAD");
  i = 1;
  while (i <= 4) {
    x = to_long(result[i]);
    if (x > 255)
      return("BIG");
    i = i + 1;
  };
  return("OK");
};

"/ips" = list("foo", "1.2.3.4 ", "1.2.333.4","1.2.3.4");
"/checks" = {
  ips = value("/ips");
  len = length(ips);
  i = 0;
  while (i < len) {
    checks[i] = check_ipv4(ips[i]);
    i = i + 1;
  };
  return(checks);
};
