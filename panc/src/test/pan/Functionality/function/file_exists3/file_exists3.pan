#
# @expect="/nlist[@name='profile']/boolean[@name='result']='false'"
# @format=pan
#
object template file_exists3;

'/result' = file_exists('nonexistant.txt');
