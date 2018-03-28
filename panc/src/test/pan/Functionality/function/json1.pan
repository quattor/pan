#
# Test json decoding
#
# @expect="/nlist[@name='profile']/boolean[@name='boolean']='true'"
# @expect="/nlist[@name='profile']/long[@name='long']='1234'"
# @expect="/nlist[@name='profile']/double[@name='double']='1234.56'"
# @expect="/nlist[@name='profile']/string[@name='string']='string'"
# @expect="/nlist[@name='profile']/list[@name='list']/*[3]='three'"
# @expect="/nlist[@name='profile']/nlist[@name='hash']/long[@name='b']='2'"
#
object template json1;

"/boolean" = json_decode("true");
"/long" = json_decode("1234");
"/double" = json_decode("1234.56");
"/string" = json_decode('"string"');
"/list" = json_decode('[1, 2, "three"]');
"/hash" = json_decode('{"a": 1, "b": 2}');
