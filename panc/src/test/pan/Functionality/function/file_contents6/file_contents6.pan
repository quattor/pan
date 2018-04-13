#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template file_contents6;

'/result' = file_contents('ok.txt.gz', 'gzip');
