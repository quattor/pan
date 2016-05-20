#
# Test JSON encoding
#
# @expect="/nlist[@name='profile']/string[@name='boolean']='true'"
# @expect="/nlist[@name='profile']/string[@name='long']='1234'"
# @expect="/nlist[@name='profile']/string[@name='double']='1234.56'"
# @expect="/nlist[@name='profile']/string[@name='string']='"string"'"
#
# Currently, there is automatic pretty-printing, but that should not be depended on.
# @expect="translate(normalize-space(/nlist[@name='profile']/string[@name='list']), ' ', '')='[1,2,"three"]'"
# @expect="translate(normalize-space(/nlist[@name='profile']/string[@name='hash']), ' ', '')='{"a":1,"b":2}'"
#
object template json2;

"/boolean" = json_encode(true);
"/long" = json_encode(1234);
"/double" = json_encode(1234.56);
"/string" = json_encode("string");
"/list" = json_encode(list(1, 2, "three"));
"/hash" = json_encode(nlist("a", 1, "b", 2));
