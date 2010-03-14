#
# @expect="/profile/result='OK'"
#
object template file_contents5;

'/result' = file_contents('path/ok.txt');
