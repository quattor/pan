#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template file_contents5;

'/result' = file_contents('path/ok.txt');
