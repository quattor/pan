#
# make sure protected resources when optimizing don't
# get changed from one profile to another
#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#

object template protected2;

include { 'set-variable-x' };

'/X' = X;
'/otherX' = value('other:/X');

'/result' = (length(value('/X')) == length(value('/otherX')));
