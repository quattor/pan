#
# @expect="/profile/result='OK'"
#
object template function3;

# Check that nested calls correctly reset the FUNCTION value.
function OK = {
  BAD();
  FUNCTION;
};

function BAD = FUNCTION;

'/result' = {
  OK();
};
