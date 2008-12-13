#
# test of index for nlists (type mismatch)
#

object template index4;

"/list" = list(nlist("name","aa", "value",11),
               nlist("name","ab", "value",21),
               nlist("name","aB", "value",31));

"/test" = index(nlist("name",0), value("/list"));
