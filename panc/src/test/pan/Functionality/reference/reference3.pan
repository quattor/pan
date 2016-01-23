#
# @expect="count(/nlist[@name='profile']/nlist[@name='result']/*)=4 and /nlist[@name='profile']/nlist[@name='result']/*[@name='a'] and /nlist[@name='profile']/nlist[@name='result']/*[@name='b'] and /nlist[@name='profile']/nlist[@name='result']/*[@name='c'] and /nlist[@name='profile']/nlist[@name='result']/*[@name='d']"
# @format=pan
#
object template reference3;

'/result' = {
  x = nlist("a",1,"b",2);
  y = x;
  y["c"] = x;
  x["d"] = 4;
  x;
};
