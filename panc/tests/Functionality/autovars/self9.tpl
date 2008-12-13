# 
# Ensure that mixed resource types will throw an
# exception when SELF updated.
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template self9;

'/result' = {
  SELF['a'][0] = 'OK';
  SELF[0]['b'] = 'BAD';
  SELF;
};
