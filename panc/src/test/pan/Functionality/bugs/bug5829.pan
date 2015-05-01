#
# @expect="/nlist[@name='profile']"
# @format=pan
#
object template bug5829;

# This comment generated errors because of these characters: éô

"/resultat" = "ôôôôôô";
