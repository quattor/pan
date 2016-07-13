# Test passing a hash as an argument
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
#

object template format10;

variable HASH = dict("entry1", 1, "entry2", 2);
variable STR = format("%s", HASH);

'/result' = {
    expected = "{ entry1, 1, entry2, 2 }";
    STR == expected;
};
