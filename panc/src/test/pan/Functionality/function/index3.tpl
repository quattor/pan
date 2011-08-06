#
# test of index() builtin
#
# @expect="/profile/name1='green' and /profile/name2=''"
#

object template index3;

# simple color table
'/table' = nlist('red', 0xf00, 'green', 0x0f0, 'blue', 0x00f);

# result will be the string 'green'
'/name1' = index(0x0f0, value('/table'));

# result will be the empty string
'/name2' = index(0x0f0, value('/table'), 1);
