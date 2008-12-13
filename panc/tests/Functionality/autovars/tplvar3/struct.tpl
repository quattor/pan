structure template struct;

'result' = {
  if (!exists(TEMPLATE)) {
    error('TEMPLATE variable does not exist');
  };
  if (TEMPLATE != 'tplvar3') {
    error('TEMPLATE variable has incorrect value');
  };
  'OK';
};
