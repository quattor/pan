# This should not cause a NPE when the empty DML statement is encountered.
#
# @expect=org.quattor.pan.parser.ParseException
#
template trac-bug-83;

'/x' = ;
