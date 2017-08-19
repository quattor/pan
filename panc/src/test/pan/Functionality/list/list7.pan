#
# @expect="/nlist[@name='profile']/list[@name='a']/*[2]=2 and /nlist[@name='profile']/long[@name='b']=2 and /nlist[@name='profile']/long[@name='c']=3 and /nlist[@name='profile']/list[@name='d']/*[2]=7"
# @format=pan
object template list7;

"/a" = list(1, 2);
"/b" = value("/a/-1");
"/c" = {
    t = list(3, 4);
    t[-2];
};
"/d" = list(5, 6);
"/d/-1" = 7;
