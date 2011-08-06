#
# make sure protected resources when optimizing don't
# get changed from one profile to another
#
# @expect="/profile/result='true'"
#

object template protected2;

include { 'set-variable-x' };

'/X' = X;
'/otherX' = value('other:/X');

'/result' = (length(value('/X')) == length(value('/otherX')));
