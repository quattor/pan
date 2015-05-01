#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template file_contents4;

'/result' = file_contents('ok.txt');
