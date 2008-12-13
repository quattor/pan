#
# @expect="/profile/result='true'"
#
object template reference7;

variable X = nlist();

'/x' = list();
'/x' = {
  SELF[0] = -1;
  SELF[1] = -1;
  SELF[0] = length(SELF);
  SELF[1] = length(value('/x'));
  SELF;
};

'/result' = ((value('/x/0')==2) && (value('/x/1')==2));
