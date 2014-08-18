#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template file_exists5;

'/result' = file_exists('path/ok.txt');
