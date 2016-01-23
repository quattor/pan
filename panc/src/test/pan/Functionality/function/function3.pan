#
# test of function recursion and parameter passing
#
# @expect="/nlist[@name='profile']/long[@name='x1']=1 and /nlist[@name='profile']/long[@name='x2']=6 and /nlist[@name='profile']/long[@name='x3']=362880"
# @format=pan
#
object template function3;

function facto = {
  if (ARGC != 1)
    error("facto(): wrong number of arguments");
  if (!is_long(ARGV[0]))
    error("facto(): not a long");
  if (ARGV[0] < 2)
    1
  else
    ARGV[0] * facto(ARGV[0] - 1);
};

"/x1" = facto(0);
"/x2" = facto(3);
"/x3" = facto(9);
