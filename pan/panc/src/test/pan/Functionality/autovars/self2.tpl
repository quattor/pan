#
# Test that self can be modified directly as long
# as self is not the direct target of an assignment.
#
# @expect="/profile/result='true'"
#
object template self2;

variable T1 = nlist('a',1);
variable T2 = nlist('a',1);
variable T3 = undef;

variable T1 = {
  h = SELF;
  h['b'] = 2;
  h;
};

variable T2 = {
  SELF['b'] = 2;
  SELF;
};

variable T3 = {
  SELF['b'] = 2;
  SELF;
};

'/result' = (length(T1)==2) && (length(T2)==2) && (length(T3)==1) && is_nlist(T3);

