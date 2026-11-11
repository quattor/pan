#
# invalid choice type
#
# @expect=org.quattor.pan.parser.ParseException
#

object template choice24;

type mychoice = choice(1,2,,);
