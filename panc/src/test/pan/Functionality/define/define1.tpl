#
# @expect="/profile/gamma=0"
#
object template define1;

function test1a = "bad";

variable test2 = "bad";

type test3a = long(1..);

function test1b = "good";

variable test2 = "good";

type test3b = long(..0);

"/alpha" = {test1a()};

"/alpha" = {test1b()};

"/beta" = test2;

bind "/gamma" = test3b;
"/gamma" = 0;

