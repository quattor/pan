#
# @expect="/profile/data1=1 and /profile/data2=1 and /profile/data3=1"
# @format=xmldb
#
object template loop5;

"/data1" = 1;
"/data2" = value("/data1");
"/data3" = value("loop5:/data1");
