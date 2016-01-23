#
# @expect="/nlist[@name='profile']/nlist[@name='result']/long[@name='a']=1 and /nlist[@name='profile']/nlist[@name='result']/string[@name='b']='OK'"
# @format=pan
#
object template iteration2;

variable X = nlist('a',1,'b','OK');

'/result' = {
  x = X;
  ok = first(x, k, v);
  while (ok) {
    r[k] = v;
    ok = next(x, k, v);
  };
  r;
};
