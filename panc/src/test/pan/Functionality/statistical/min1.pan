#
# @expect="/nlist[@name='profile']/double[@name='double']=15.2 and /nlist[@name='profile']/long[@name='long']=1096 and /nlist[@name='profile']/double[@name='mixed']=219.0"
# @format=pan
#

object template min1;

"/double" = min(65.15, 15.2);
"/long" = min(1096, 2034);
"/mixed" = min(219, 351.5);
