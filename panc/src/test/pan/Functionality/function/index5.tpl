#
# test of index() builtin
#
# @expect="/nlist[@name='profile']/string[@name='nn1']='b' and /nlist[@name='profile']/string[@name='nn2']='d'"
# @format=pan
#

object template index5;

'/nn1' = index(
              nlist('key', 'foo'),
              nlist(
                   'a', nlist('key', 'bar', 'val', 101),
                   'b', nlist('key', 'foo')
                  )
             );

'/nn2' = index(
              nlist('key', 'foo'),
              nlist(
                   'a', nlist('key', 'bar', 'val', 101),
                   'b', nlist('key', 'foo'),
                   'c', nlist('key', 'bar', 'val', 101),
                   'd', nlist('key', 'foo'),
                   'e', nlist('key', 'bar', 'val', 101),
                   'f', nlist('key', 'foo')
                  ),
              1
             );