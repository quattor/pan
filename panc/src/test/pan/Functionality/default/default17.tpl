#
# @expect="/nlist[@name='profile']/list[@name='result']/*[1]/boolean[@name='a']='true' and /nlist[@name='profile']/list[@name='result']/*[1]/double[@name='b']=1.2 and /nlist[@name='profile']/list[@name='result']/*[2]/boolean[@name='a']='true' and /nlist[@name='profile']/list[@name='result']/*[2]/double[@name='b']=3.4"
# @format=pan
#
object template default17;

type t = extensible {
  'a' : boolean = true
};

bind '/result' = t[];

'/result/0/b' = 1.2;
'/result/1/b' = 3.4;
