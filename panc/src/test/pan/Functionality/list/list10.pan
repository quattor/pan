# get index greater than size
#
# @expect=org.quattor.pan.exceptions.EvaluationException ".*variable named 't\[3\]' is undefined.*"
#
object template list10;

bind "/a" = string = 'test';

"/a" = {
    t = list(1 ,2);
    # no autovivification of t[3] to undef
    # so exception thrown instead of valid compile with schema default
    t[3];
};
