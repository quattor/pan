# @expect="/nlist[@name="profile"]/long[@name="a"]=0 and /nlist[@name="profile"]/long[@name="value"]=0 and /nlist[@name="profile"]/long[@name="default"]=1"
# @format=pan
#
object template value2;

variable SOMETHING = dict("key", "abc");

"/a" = 0;
"/value" = value("/a", 1);
"/default" = value("/nopath", 1);
