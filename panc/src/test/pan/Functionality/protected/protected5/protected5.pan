#
# make sure protected resources when optimizing don't
# get changed from one profile to another
#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#

object template protected5;

include { 'set-path-x' };

'/result' = (value('/X/a') == value('other:/X/a'))
            &&  (value('/X/b') == value('other:/X/b'));
