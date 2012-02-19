#
# @expect="/profile/result/a/gamma='a' and /profile/result/b/gamma='b'"
# @format=xmldb
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
