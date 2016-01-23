
#
# Negative values in ranges should be OK now.
#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
#

object template bug-sf-3489988;

type deca = long(-5..-3);
type decb = long(-5..);
type decc = long(-5..3);

type octa = long(-05..-03);
type octb = long(-05..);
type octc = long(-05..03);

type hexa = long(-0x5..-0x3);
type hexb = long(-0x5..);
type hexc = long(-0x5..0x3);

'/result' = 'OK';
