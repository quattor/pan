#
# @expect="/nlist[@name='profile']/list[@name='result']/*[1]/long[@name='index']=0 and /nlist[@name='profile']/list[@name='result']/*[2]/long[@name='index']=1 and /nlist[@name='profile']/list[@name='result']/*[3]/long[@name='index']=2"
# @format=pan
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

