#
# test of template autoloading a structure template
#
# @expect="/profile/result/a=1 and /profile/result/b=2"
# @format=xmldb
#

object template autoload_structure;

'/result' = create('struct');
