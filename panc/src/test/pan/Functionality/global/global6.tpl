#
# see if globals can be modified indirectly via iterators in dml
#
# @expect="/nlist[@name='profile']/list[@name='data']/*[1]/string[@name='a']='OK'"
#
object template global6;

variable GLOBAL = list(nlist("a", "OK"));

variable GLOBAL = {
    foreach (idx; item; GLOBAL) {
        item["a"] = "BAD";
    };
    SELF;
};

"/data" = GLOBAL;
