#
# test of index() builtin
#
# @expect="/nlist[@name='profile']/long[@name='ll1']=1 and /nlist[@name='profile']/long[@name='ll2']=1"
# @format=pan
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
