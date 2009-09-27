#
# @expect="/profile/result='OK'"
#
object template file_contents4;

'/result' = file_contents('ok.txt');
