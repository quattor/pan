#
# @expect="/profile/y='a' and /profile/z='b'"
# @format=xmldb
#
object template key1;
"/x/a" = 1;
"/x/b" = 2;

"/y" = key (value("/x"),0);
"/z" = key (value("/x"),1);