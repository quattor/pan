#
# test of iteration with first/next
#
# @expect="/nlist[@name='profile']/list[@name='test1_list']/*[1]='012' and /nlist[@name='profile']/list[@name='test1_list']/*[2]='aaabbbccc' and /nlist[@name='profile']/list[@name='test1_nlist']/*[1]='abc' and /nlist[@name='profile']/list[@name='test1_nlist']/*[2]='111222333' and /nlist[@name='profile']/list[@name='test2_list']/*[1]='012' and /nlist[@name='profile']/list[@name='test2_list']/*[2]='aaabbbccc' and /nlist[@name='profile']/list[@name='test2_nlist']/*[1]='abc' and /nlist[@name='profile']/list[@name='test2_nlist']/*[2]='111222333'"
# @format=pan
#

object template key5;

function key5_join1 = {
  keys = values = "";
  ok = first(ARGV[0], k, v);
  while (ok) {
    keys   = keys   + to_string(k);
    values = values + to_string(v);
    ok = next(ARGV[0], k, v);
  };
  return(list(keys, values));
};

function key5_join2 = {
  keys = values = "";
  ok = first(ARGV[0], k, undef);
  while (ok) {
    keys = keys + to_string(k);
    ok = next(ARGV[0], k, undef);
  };
  ok = first(ARGV[0], undef, v);
  while (ok) {
    values = values + to_string(v);
    ok = next(ARGV[0], undef, v);
  };
  return(list(keys, values));
};

"/test1_list" = key5_join1(list("aaa", "bbb", "ccc"));
"/test1_nlist" = key5_join1(nlist("a", 111, "b", 222, "c", 333));

"/test2_list" = key5_join2(list("aaa", "bbb", "ccc"));
"/test2_nlist" = key5_join2(nlist("a", 111, "b", 222, "c", 333));
