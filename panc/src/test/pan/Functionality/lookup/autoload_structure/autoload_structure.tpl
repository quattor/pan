#
# test of template autoloading a structure template
#
# @expect="/nlist[@name='profile']/nlist[@name='result']/long[@name='a']=1 and /nlist[@name='profile']/nlist[@name='result']/long[@name='b']=2"
# @format=pan
#

object template autoload_structure;

'/result' = create('struct');
