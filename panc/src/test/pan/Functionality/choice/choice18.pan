#
# @expect="/nlist[@name='profile']/string[@name='x']='aaa'"
#

object template choice18;

# Each entry on a new line, but no trailing comma
type mychoice = choice(
    "aa",
    "bb",
    "aaa",
    "bbb"
);
bind '/x' = mychoice with length(SELF) == 3;
'/x' = "aaa";
