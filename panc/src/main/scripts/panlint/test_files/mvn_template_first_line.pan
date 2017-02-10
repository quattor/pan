${componentconfig}

include 'components/accounts/functions';

# Include system users and groups which shouldn't be removed
# by default.  The machine configuration can still modify or
# remove them manually.
include 'components/accounts/sysgroups';
include 'components/accounts/sysusers';
