#
# test of string escaping
#
# @expect="count(/nlist[@name='profile']/nlist[@name='test']/*)=8"
# @format=pan
#

object template strings11;

"/test" = {
  test = list("", " ", "1", "x", "1x", "x1", "1+2=3", "abc");
#  test = list("", " ", "1", "x", "1x", "x1", "1+2=3", "\x80\xa4\xee");
  x = 0;
  while (x < length(test)) {
    esc = escape(test[x]);
    str = unescape(esc);
    if (str != test[x])
      error("failed to escape " + test[x]);
    result[esc] = str;
    x = x +1;
  };
  return(result);
};
