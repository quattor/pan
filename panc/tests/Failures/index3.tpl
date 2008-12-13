#
# test of index for nlists
#

object template index3;

"/list" = list(nlist("name","aa", "value",11),
               nlist("name","ab", "value",21),
               nlist("name","aB", "value",31));

"/test" = index(nlist("name","ab"), value("/list"));
