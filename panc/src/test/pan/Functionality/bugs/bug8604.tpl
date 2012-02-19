#
# @expect="/nlist[@name='profile']"
# @format=pan
#
object template bug8604;

function sindes_add = {

  if (ARGV[0] == "item5"){
    regex_bis = "(^|.*,)" + ARGV[0] + "(,.*)*$";
    regex_man = "(^|.*,)item5(,.*)*$";

    string_to_test = "item99,item2,item3,item4";

    if(regex_bis == regex_man) {

       item5_bis = match(string_to_test, regex_bis);
       item5_man = match(string_to_test, regex_man);

       if (to_string(item5_bis) != to_string(item5_man)) {
         error("regex matches, bis:" + to_string(item5_bis) + ", man:" + to_string(item5_man));
       };

    } else {
       error("regex's don't match:" + regex_bis + ", " + regex_man);
    };

  };

  regex = "(^|.*,)" + (ARGV[0]) + "(,.*)*$";
  if (! match("1", regex)) {
    v=1;
  };

  return("nothing");
};

"/result" = sindes_add("item3");
"/result" = sindes_add("item4");
"/result" = sindes_add("item5");
