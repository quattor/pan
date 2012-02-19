#
# @expect="/nlist[@name='profile']/string[@name='y']='a' and /nlist[@name='profile']/string[@name='z']='b'"
# @format=pan
#
object template key1;
"/x/a" = 1;
"/x/b" = 2;

"/y" = key (value("/x"),0);
"/z" = key (value("/x"),1);