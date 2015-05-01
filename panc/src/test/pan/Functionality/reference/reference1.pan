#
# @expect="/nlist[@name='profile']/nlist[@name='result']/nlist[@name='a']/string[@name='gamma']='a' and /nlist[@name='profile']/nlist[@name='result']/nlist[@name='b']/string[@name='gamma']='b'"
# @format=pan
#
object template reference1;

variable X = nlist("a",nlist("alpha",1),"b",nlist("beta",2));

'/result' = {
  x = X;
  ok = first(x, k, v);
  while(ok) {
    v["gamma"] = k;
    ok = next(x, k, v);
  };
  x;
};
