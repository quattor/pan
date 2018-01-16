#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template debug4;

'/result' = {
    debug("Hello %s", "quattor");
    true;
};
