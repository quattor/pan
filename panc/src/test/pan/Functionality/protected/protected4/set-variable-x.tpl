template set-variable-x;

variable X ?= nlist('a', 1);

variable X = {
  SELF['b'] = length(SELF);
  SELF;
};

