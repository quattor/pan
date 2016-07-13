# Test passing list as an argument
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
#

object template format8;

variable LIST = list(1, 2, 3);
variable STR = format("%s", LIST);

'/result' = {
    expected = "[ 1, 2, 3 ]";
    STR == expected;
};
