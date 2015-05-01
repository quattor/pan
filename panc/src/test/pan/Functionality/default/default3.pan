#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template default3; 

"/result" = "OK";

# The DML should not be run at all.  If it
# is this will fail.
"/result" ?= value("xxx:/data");

