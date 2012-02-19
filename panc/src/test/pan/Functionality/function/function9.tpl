#
# test to_boolean function
#
# @expect="/nlist[@name='profile']/boolean[@name='t1']='true' and /nlist[@name='profile']/boolean[@name='t1']='true' and /nlist[@name='profile']/boolean[@name='t3']='true' and /nlist[@name='profile']/boolean[@name='t4']='true' and /nlist[@name='profile']/boolean[@name='f1']='false' and /nlist[@name='profile']/boolean[@name='f2']='false' and /nlist[@name='profile']/boolean[@name='f3']='false' and /nlist[@name='profile']/boolean[@name='f4']='false'"
# @format=pan
#

object template function9;

"/t1" = to_boolean("hello");
"/t2" = to_boolean(123);
"/t3" = to_boolean(4.56);
"/t4" = to_boolean(true);

"/f1" = to_boolean("");
"/f2" = to_boolean(0);
"/f3" = to_boolean(0.0);
"/f4" = to_boolean(false);
