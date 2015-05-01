#
# @expect="/nlist[@name='profile']/list[@name='result']/*[1]=1 and /nlist[@name='profile']/list[@name='result']/*[2]='OK'"
# @format=pan
#
object template iteration3;

variable X = list(1,'OK');

'/result' = {
  foreach(k; v; X) {
    r[k] = v;
  };
  r;
};
