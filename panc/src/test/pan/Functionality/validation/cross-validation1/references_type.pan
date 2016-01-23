
declaration template references_type;

# This type requires that the value is an nlist of
# longs.  Each key in the nlist must refer to another
# machine and the path machine:/others/OBJECT must exist.

type references_type = long{} with {
  foreach (host; value; SELF) {
    remote_path = host + ':/others/' + OBJECT;
    debug('checking: ' + remote_path);
    if (!exists(remote_path+'')) {
      error(remote_path + ' does not exist');
    };
  };
  true;
};
