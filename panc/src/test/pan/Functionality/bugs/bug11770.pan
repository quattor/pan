#
# @expect="/nlist[@name='profile']/boolean[@name='list']='true' and /nlist[@name='profile']/boolean[@name='nlist']='true'"
# @format=pan
#
object template bug11770;
"/list" = is_list(merge(list(),list()));
"/nlist" = is_nlist(merge(nlist(),nlist()));
