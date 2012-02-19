#
# @expect="/nlist[@name='profile']/long[@name='data1']=1 and /nlist[@name='profile']/long[@name='data2']=1 and /nlist[@name='profile']/long[@name='data3']=1"
# @format=pan
#
object template loop5;

"/data1" = 1;
"/data2" = value("/data1");
"/data3" = value("loop5:/data1");
