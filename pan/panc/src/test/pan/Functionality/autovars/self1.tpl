#
# Test that self can be modified directly as long
# as self is not the direct target of an assignment.
#
# @expect="/profile/result='true'"
#
object template self1;

variable T1 = list('a','b');
variable T2 = list('a','b');
variable T3 = undef;

variable T1 = {
  lst = SELF;
  lst[length(lst)] = 'c';
  lst;
};

variable T2 = {
  SELF[length(SELF)] = 'c';
  SELF;
};

variable T3 = {
  SELF[length(SELF)] = 'c';
  SELF;
};

'/result' = (length(T1)==3) && (length(T2)==3) && (length(T3)==1) && is_list(T3);

