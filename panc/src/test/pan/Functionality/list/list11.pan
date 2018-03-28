# append + prefix with -1
#
# @expect="/nlist[@name='profile']/list[@name='d']/*[2]/long[@name='a']=6 and /nlist[@name='profile']/list[@name='d']/*[2]/long[@name='b']=7"
# @format=pan
object template list11;

"/d" = list(dict());
"/d" = append(dict('a', 6));
prefix "/d/-1";
"b" = 7;
