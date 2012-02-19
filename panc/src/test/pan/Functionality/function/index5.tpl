#
# test of index() builtin
#
# @expect="/profile/nn1='b' and /profile/nn2='d'"
# @format=xmldb
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