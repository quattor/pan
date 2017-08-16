# @expect="/nlist[@name='profile']/boolean[@name='undef']='true' and /nlist[@name='profile']/boolean[@name='null']='true'"
# @format=pan
#
object template value3;

"/undef" = !is_defined(value("/nopath", undef));
"/null" = is_null(value("/nopath", null));
