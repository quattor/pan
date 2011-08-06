#
# @expect="/profile/result='true'"
#
object template split1;

variable TVALUE = list('a', 'b', 'c');
variable X = split('\s*,\s*', 0, 'a,b , c,,');
variable Y = split('\s*,\s*', 'a,b , c,,');

variable T1 = { length(X) == 3 && length(Y) == 3 };
variable T2 = {
  foreach(k; v; TVALUE) {
    if (v != X[k]) {
      return(false);
    };
  };
  true;
};
variable T3 = {
  foreach(k; v; TVALUE) {
    if (v != Y[k]) {
      return(false);
    };
  };
  true;
};

'/result' = T1 && T2 && T3;


