#
# @expect="/nlist[@name='profile']/string[@name='a']='0,1,-2,3'"
# @format=pan
object template list12;

"/a" = {
    l = list(0, 1);
    # autovivification of l[2] = undef
    l[3] = 3;
    l2 = list();
    foreach(idx; v; l) {
        if (!is_defined(v)) {
            v = -idx;
        };
        l2[idx] = to_string(v);
    };
    join(",", l2);
};
