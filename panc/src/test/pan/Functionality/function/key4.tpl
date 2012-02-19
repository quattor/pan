#
# test key within a loop
#
# @expect="/nlist[@name='profile']/string[@name='keys']='blue green red'"
# @format=pan
#
object template key4;

"/table" = nlist("red", 0xf00, "green", 0x0f0, "blue", 0x00f);
"/keys" = {
  tbl = value("/table");
  res = "";
  len = length(tbl);
  idx = 0;
  while (idx < len) {
    res = res + key(tbl, idx) + " ";
    idx = idx + 1;
  };
  if (length(res) > 0)
    res=splice(res, -1, 1);
  return(res);
};
