#
# make sure protected resources when optimizing don't
# get changed from one profile to another
#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#

object template protected3;

include { 'set-path-x' };

'/size1' = length(value('/X'));
'/size2' = length(value('other:/X'));

'/result' = (value('/size1') == value('/size2'));
