#
# test of numerical operators
#
# @expect="/nlist[@name='profile']/list[@name='n']/*[1]=0 and /nlist[@name='profile']/list[@name='n']/*[2]>0.0 and /nlist[@name='profile']/list[@name='n']/*[3]>0.0 and /nlist[@name='profile']/list[@name='n']/*[4]>0.0 and /nlist[@name='profile']/list[@name='n']/*[5]=0.0 and /nlist[@name='profile']/list[@name='n']/*[6]=0.0"
# @format=pan
#

object template operator2;

"/n/0" = 6   / 7  ;
"/n/1" = 6.0 / 7  ;
"/n/2" = 6   / 7.0;
"/n/3" = 6.0 / 7.0;

"/n/4" = (1.0 / 3) + (2.0 / 3) - 1;
"/n/5" = (1.0 / 7) + (2.0 / 7) + (4.0 / 7) - 1;
