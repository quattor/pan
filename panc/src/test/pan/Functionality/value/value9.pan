# @expect="/nlist[@name="profile"]/long[@name="undef"]=0 and /nlist[@name="profile"]/long[@name="value"]=0"
# @format=pan
#
object template value9;

bind "/undef" = long = 0;

"/undef" = undef;
"/value" = value("/undef", 5);
