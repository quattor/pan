#
# @expect="/nlist[@name='profile']/double[@name='double']=130.48 and /nlist[@name='profile']/long[@name='long']=1743 and /nlist[@name='profile']/double[@name='mixed']=7495.0"
# @format=pan
#

object template max1;

"/double" = max(44.27, 130.48);
"/long" = max(1743, 85);
"/mixed" = max(7495, 222.9936);
