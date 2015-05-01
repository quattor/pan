#
# simple tests of regexps
#
# @expect="/nlist[@name='profile']/boolean[@name='s1']='true' and /nlist[@name='profile']/boolean[@name='s2']='false' and count(/nlist[@name='profile']/list[@name='s3']/*)=5 and count(/nlist[@name='profile']/list[@name='s4']/*)=3 and /nlist[@name='profile']/list[@name='s4']/*[2]='16.' and /nlist[@name='profile']/boolean[@name='s5']='false' and /nlist[@name='profile']/boolean[@name='s6']='true' and count(/nlist[@name='profile']/list[@name='s7']/*)=3 and /nlist[@name='profile']/list[@name='s7']/*[2]='dd'"
# @format=pan
#
object template regexp1;

"/s1" = match("foo", 'o');
"/s2" = match("foo", 'x');
"/s3" = matches("137.138.16.5", '^(\d+)\.(\d+)\.(\d+)\.(\d+)$');
"/s4" = matches("137.138.16.5", '^(\d+\.){3}(\d+)$');

# play with case
"/s5" = match("HelLo", '^hello$');
"/s6" = match("HelLo", '^(?i)hello$');

# back references: we want a doubled letter appearing twice
"/s7" = matches("abbcddef aabcddeff", '((\w)\2).+\1');
