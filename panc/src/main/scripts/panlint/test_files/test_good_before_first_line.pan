# ${license-info}
# ${developer-info}
# ${author-info}

declaration template components/openstack/catalog;

include 'components/openstack/catalog/murano';

@documentation {
Type to define OpenStack catalog services
}
type openstack_catalog_config = {
    'murano' ? openstack_murano_config
} with openstack_oneof(SELF, 'murano');
