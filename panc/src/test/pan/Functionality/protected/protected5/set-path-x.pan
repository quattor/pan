template set-path-x;

'/X' = nlist('a', 1);

'/X' = {
  SELF['b'] = length(SELF);
  SELF;
};

