#
# simple tests of regexps
#
# @expect="/profile/s1='true' and /profile/s2='false' and count(/profile/s3)=5 and count(/profile/s4)=3 and /profile/s4[2]='16.' and /profile/s5='false' and /profile/s6='true' and count(/profile/s7)=3 and /profile/s7[2]='dd'"
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
