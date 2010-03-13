#
# test of function loop
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template loop2;

function bogus1 = {
    bogus2(1);
};

function bogus2 = {
    bogus1(2);
};

"/x" = bogus1(1) + bogus2(2);
