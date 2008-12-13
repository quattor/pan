#
# missing end tag because of the extra space...
#
# @expect=org.quattor.pan.parser.ParseException
#

template bad;

"/x" = <<EOT;
xx
yy
 EOT
