#
# @expect="/profile/result=0"
# @format=xmldb
#

object template bug-trac-171;

variable X = 'OK';

'/result' = {
 create('struct');
 length(SELF);
};
