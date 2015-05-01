#
# make sure that to_long() understands the different bases
#
# @expect="/nlist[@name='profile']/long[@name='x1']=12 and /nlist[@name='profile']/long[@name='x2']=10 and /nlist[@name='profile']/long[@name='x3']=18 and /nlist[@name='profile']/long[@name='y1']=12 and /nlist[@name='profile']/long[@name='y2']=10 and /nlist[@name='profile']/long[@name='y3']=18"
# @format=pan
#

object template function15;

"/x1" = to_long("12");		# 12 (decimal)
"/x2" = to_long("012");		# 10 (octal)
"/x3" = to_long("0x12");	# 18 (hexadecimal)

"/y1" = 12;
"/y2" = 012;
"/y3" = 0x12;
