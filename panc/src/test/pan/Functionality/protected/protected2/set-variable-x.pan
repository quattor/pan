template set-variable-x;

variable X ?= list(1);

variable X = {
  SELF[length(SELF)] = 2;
  SELF;
};

