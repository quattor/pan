#
# tests of list creation with list(), merge() and nlist()
#
# @expect="/nlist[@name='profile']"
# @format=pan
#

object template list2;

"/l1" = list(1, 2, 3);
"/l2" = list (list(11, 12, 13), value("/l1"));
"/l3" = merge(list(11, 12, 13), value("/l1"));
"/l4" = merge(list(11, 12, 13), list(), value("/l1"));

"/n1" = nlist(
  "red",   0x00f,
  "green", 0x0f0,
  "blue",  0xf00,
);
"/n2" = merge(value("/n1"), nlist("magenta", 123, "cyan", 456));
