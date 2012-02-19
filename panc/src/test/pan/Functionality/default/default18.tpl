#
# @expect="/nlist[@name='profile']/nlist[@name='result']/nlist[@name='alpha']/boolean[@name='a']='true' and /nlist[@name='profile']/nlist[@name='result']/nlist[@name='alpha']/double[@name='b']=1.2 and /nlist[@name='profile']/nlist[@name='result']/nlist[@name='beta']/boolean[@name='a']='true' and /nlist[@name='profile']/nlist[@name='result']/nlist[@name='beta']/double[@name='b']=3.4"
# @format=pan
#
object template default18;

type t = extensible {
  'a' : boolean = true
};

bind '/result' = t{};

'/result/alpha/b' = 1.2;
'/result/beta/b' = 3.4;
