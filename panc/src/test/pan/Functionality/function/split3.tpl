#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template split3;

variable TVALUE = list('a', 'b , c,,');
variable X = split('\s*,\s*', 2, 'a,b , c,,');

variable T1 = { length(X) == 2 };
variable T2 = {
  foreach(k; v; TVALUE) {
    if (v != X[k]) {
      return(false);
    };
  };
  true;
};

'/result' = T1 && T2;


