#
# JSON: test the behavior of null
#
# @expect="/nlist[@name='profile']/nlist[@name='data']/long[@name='a']='1'"
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @expect="translate(normalize-space(/nlist[@name='profile']/string[@name='data2']), ' ', '')='[1,null]'"
# @expect="translate(normalize-space(/nlist[@name='profile']/string[@name='data3']), ' ', '')='{"a":1}'"
# @expect="/nlist[@name='profile']/string[@name='data4']='null'"
#
object template json3;

"/data" = json_decode('{"a": 1, "b": null}');
"/result" = !exists("/data/b");
"/data2" = json_encode(list(1, null));
"/data3" = json_encode(nlist("a", 1, "b", null));
"/data4" = json_encode(null);
