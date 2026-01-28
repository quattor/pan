template bob.org;

variable WHYYY = -102;

# panlint disable=LP011,LC001
# we think we know better than panlint
variable x= bob[-4];

# this is a comment explaining that bob is a great host
# panlint disable=PP001
'/system/hostname/' = 'bob.foo.org';

# panlint disable=CU001,
'/software/components/chkconfig/service/rdma' = dict();

# panlint disable=CU001

prefix '/software/components/metaconfig/services/{/etc/sysconfig/fetch-crl}';

# panlint disable=LP006
# We know this line is long, but it's unavoidable
variable SUPER_DUPER_LONG_LINE = 'sdfffffffffffffffffffffffffffffffffffffffffffffffffffffffffff892342342342943  234902342934 90234 90234 234 44444444444444444444444444444444444444444444';
