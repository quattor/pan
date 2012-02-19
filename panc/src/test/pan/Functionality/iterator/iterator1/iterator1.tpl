#
# @expect="/profile/result[1]/index=0 and /profile/result[2]/index=1 and /profile/result[3]/index=2"
# @format=xmldb
#
object template iterator1;

variable X = list('a','b','c');

variable Y = {
  info = list();
  x = X;
  ok = first(x,k,v);
  while (ok) {
    info[length(info)] = create('struct', 'index', k);
    ok = next(x,k,v);
  };
  info;
};

'/result' = Y;

