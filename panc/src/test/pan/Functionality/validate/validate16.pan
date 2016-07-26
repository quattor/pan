#
# @expect="/nlist[@name='profile']/boolean[@name='res']='false'"
#

object template validate16;

type mytype = {
  'entry1': string(3..)
  'entry2': long
};

variable X = dict('entry1', "Ha", 'entry2', 1);

'/res' = validate(mytype, X);
