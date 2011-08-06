#
# @expect="/profile/result='true'"
#
object template split2;

variable TVALUE = list('a', 'b', 'c', '', '');
variable X = split('\s*,\s*', -1, 'a,b , c,,');

variable T1 = { length(X) == 5 };
variable T2 = {
  foreach(k; v; TVALUE) {
    if (v != X[k]) {
      return(false);
    };
  };
  true;
};

'/result' = T1 && T2;


