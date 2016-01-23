#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template file_exists4;

'/result' = file_exists('ok.txt');
