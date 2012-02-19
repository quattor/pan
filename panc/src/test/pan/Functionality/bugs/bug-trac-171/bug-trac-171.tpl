#
# @expect="/nlist[@name='profile']/long[@name='result']=0"
# @format=pan
#

object template bug-trac-171;

variable X = 'OK';

'/result' = {
 create('struct');
 length(SELF);
};
