#
# @expect="/profile/list='true' and /profile/nlist='true'"
# @format=xmldb
#
object template bug11770;
"/list" = is_list(merge(list(),list()));
"/nlist" = is_nlist(merge(nlist(),nlist()));
