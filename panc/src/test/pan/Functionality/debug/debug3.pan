#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template debug3;

'/result' = {
    debug("Hello quattor");
    true;
};
