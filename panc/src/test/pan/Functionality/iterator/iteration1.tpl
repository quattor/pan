#
# @expect="/nlist[@name='profile']/nlist[@name='result']/long[@name='a']='1' and /nlist[@name='profile']/nlist[@name='result']/string[@name='b']='OK'"
# @format=pan
#
object template iteration1;

variable X = nlist('a',1,'b','OK');

'/result' = {
  foreach(k; v; X) {
    r[k] = v;
  };
  r;
};
