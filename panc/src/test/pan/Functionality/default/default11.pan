#
# @expect="/nlist[@name='profile']/long[@name='result']=2"
# @format=pan
#
object template default11;

type xtype = long(0..10) = {x=2; y=2; x+y;} with (SELF % 2 == 0);

bind '/result' = xtype;

'/result' = 2;

 