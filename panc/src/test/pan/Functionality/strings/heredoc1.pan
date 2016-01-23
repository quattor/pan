# A heredoc string with a beginning empty line used to cause
# a null pointer exception in the parser.
#
# @expect="/nlist[@name='profile']"
# @format=pan
#
object template heredoc1;

variable CONTENTS = <<EOF;

EOF
