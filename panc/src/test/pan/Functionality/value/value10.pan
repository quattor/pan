# @expect="/nlist[@name="profile"]/nlist[@name="base"]/long[@name="a"]=40 and /nlist[@name="profile"]/nlist[@name="value"]/long[@name="a"]=0 and /nlist[@name="profile"]/nlist[@name="base"]/long[@name="b"]=30 and /nlist[@name="profile"]/nlist[@name="value"]/long[@name="b"]=30 and /nlist[@name="profile"]/nlist[@name="base"]/long[@name="c"]=50 and /nlist[@name="profile"]/nlist[@name="value"]/long[@name="c"]=50"
# @format=pan
#
object template value10;

type x = {
    'a' : long = 0
    'b' : long = 10
    'c' : long = 20
};
bind "/base" = x;

"/base/c" ?= 50;

"/base/b" = 30;
"/value" = value("/base");

# value and bind default do not change the actual value of /value
# tested by default assignment
"/base/a" ?= 40;
