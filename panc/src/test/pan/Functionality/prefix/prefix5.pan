#
# @expect="/nlist[@name='profile']/nlist[@name='a']/long[@name='la']=1 and /nlist[@name='profile']/nlist[@name='a']/nlist[@name='b']/nlist[@name='c']/long[@name='lbc']=1 and /nlist[@name='profile']/nlist[@name='a']/nlist[@name='d']/long[@name='ld']=1 and /nlist[@name='profile']/nlist[@name='e']/long[@name='le']=1 and /nlist[@name='profile']/nlist[@name='e']/nlist[@name='f']/nlist[@name='g']/long[@name='lfg']=1"
# @format=pan
#

object template prefix5;

prefix '/a';

'la' = 1;

# relative to /a
prefix 'b/c';
'lbc' = 1;

# still relative to /a
prefix 'd';
'ld' = 1;

# reset absolute prefix
prefix '/e';
'le' = 1;

# relative to /e
prefix 'f/g';
'lfg' = 1;
