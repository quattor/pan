#
# @expect="/profile/result1='OK' and /profile/result2='OK' and /profile/result3='OK' and /profile/result4='OK'"
#
object template loadpath2; 

variable LOADPATH = {
  lst = SELF;
  lst[length(lst)] = "test";
  lst[length(lst)] = "one";
  lst[length(lst)] = "two";
  return(lst);
};

include {'test2'};

include {'alpha'};
include {'beta'};
include {'gamma'};