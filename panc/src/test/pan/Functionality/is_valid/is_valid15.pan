#
# @expect="/nlist[@name='profile']/boolean[@name='res']='true'"
#

object template is_valid15;

type mytype = {
  'entry1': string(3..)
  'entry2': long
};

variable X = dict('entry1', "Hallo", 'entry2', 1);

'/res' = is_valid(mytype, X);
