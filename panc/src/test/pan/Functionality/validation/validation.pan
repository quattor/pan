#
# @expect="/nlist[@name='profile']"
# @format=pan
#
object template validation;

function foo = {1};

type uint = long with SELF >= 0;
type longlist = long[4..];
type longrec  = {"a":long "b":long} with SELF["a"] < SELF["b"];
type optrec   = {"a":long "b"?long};

bind "/x" = uint;
"/x" = 1;

"/y" = 2;
bind "/y" = uint;

bind "/z" = longrec;
"/z/a" = foo();
"/z/b" = 2;

bind "/a" = longlist;
"/a/0" = 1;
"/a/1" = 2;
"/a/2" = 3;
"/a/3" = 4;

valid "/b" = SELF >= 0;
"/b" = 1;

bind "/c" = optrec;
"/c/a" = 1;
#"/c/b" = 2; # ok if missing
#"/c/c" = 3; # would cause an error if present
