#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template tostring1;

variable X = list('a', 'b', 'c');
variable Y = nlist('a', 1, 'b', 2, 'c', 3);

'/t1' = to_string(X);
'/t2' = to_string(Y);
'/t3' = to_string(undef);
'/t4' = to_string(3);

# If nothing caused an error, then everything's OK.
'/result' = 'OK';

