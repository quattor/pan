# Test double
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
#

object template format12;

variable STR = format("%.2f", 1.15);

'/result' = {
    expected = "1.15";
    STR == expected;
};
