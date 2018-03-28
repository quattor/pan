# @expect="/nlist[@name='profile']/nlist[@name='var']/string[@name='key']='abc' and /nlist[@name='profile']/nlist[@name='othervar']/string[@name='key']='xyz' and /nlist[@name='profile']/nlist[@name='origvar']/string[@name='key']='abc'"
# @format=pan
#
object template value4;

variable SOMETHING = dict("key", "abc");

"/var" = value("/nopath", SOMETHING);
# test copy of variable
"/othervar" = {
    t = value("/nopath", SOMETHING);
    # modify the value, should not modify the original variable
    t['key'] = 'xyz';
    t;
};
"/origvar" = SOMETHING;
