unique template npush;

function npush = {

# Dummy test.
#   nlist();

# Just use self.
#  self[argv[0]] = argv[1];
#  self[argv[0]] = argv[1];
#  self;

# Original code.
   if ( argc != 2 ) {
      error("usage: 'full/path' = npush(key , value)");
   };

   # If the path does exist, add the new entry if it does not exist yet.
   if (exists(self) && is_nlist(self)) {
      v = self;
      if (!exists(v[argv[0]]) ) {
         v[argv[0]] = argv[1];
         return(v);
      };
      error("entry " + argv[0] + " already exists");
   };

   # If the path does not exist or is undefined, make new list.
   if (!exists(self) || !is_defined(self)) {
      v = nlist(argv[0], argv[1]);
      return(v);
   };

   # If we got here, self exists and is defined but is not a nlist.
   error("npush can only be applied to a nlist");

};
