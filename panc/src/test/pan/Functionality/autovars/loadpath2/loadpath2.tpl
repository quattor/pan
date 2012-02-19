#
# @expect="/nlist[@name='profile']/string[@name='result1']='OK' and /nlist[@name='profile']/string[@name='result2']='OK' and /nlist[@name='profile']/string[@name='result3']='OK' and /nlist[@name='profile']/string[@name='result4']='OK'"
# @format=pan
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