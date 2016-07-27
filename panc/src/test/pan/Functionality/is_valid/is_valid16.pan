#
# @expect="/nlist[@name='profile']/boolean[@name='res']='false'"
#

object template is_valid16;

type mytype = {
  'entry1': string(3..)
  'entry2': long
};

variable X = dict('entry1', "Ha", 'entry2', 1);

'/res' = is_valid(mytype, X);
