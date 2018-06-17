# @expect="/nlist[@name="profile"]/long[@name="undef"]=0 and /nlist[@name="profile"]/long[@name="value"]=5"
# @format=pan
#
object template value9b;

"/undef" = undef;
"/value" = value("/undef", 5);

# bind after value has no effect
bind "/undef" = long = 0;
