#
# test of index() builtin
#
# @expect="/profile/ll1=1 and /profile/ll2=1"
#

object template index4;

'/ll1' = index(
              nlist('key', 'foo'),
              list(
                   nlist('key', 'bar', 'val', 101),
                   nlist('key', 'foo')
                  )
             );

'/ll2' = index(
              nlist('key', 'foo'),
              list(
                   nlist('key', 'bar', 'val', 101),
                   nlist('key', 'foo'),
                   nlist('key', 'bar', 'val', 101),
                   nlist('key', 'foo'),
                   nlist('key', 'bar', 'val', 101),
                   nlist('key', 'foo')
                  ),
              1
             );
