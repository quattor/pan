# This file has DOS style line endings.  This
# caused an "EOF" not found error on older 
# versions of the compiler.
# (savannah bug #21115)
#
# @expect="/nlist[@name='profile']/string[@name='result']"
# @format=pan
#
object template heredoc2;

'/result' = <<EOF;
something
EOF
