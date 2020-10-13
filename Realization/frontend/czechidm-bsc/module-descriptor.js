module.exports = {
  id: 'bsc',
  npmName: 'czechidm-bsc',
  backendId: 'bsc',
  name: 'CzechIdM Module for Business card',
  description: 'CzechIdM Module for Business card',
  // 'mainStyleFile': 'src/css/main.less',
  mainRouteFile: 'routes.js',
  mainComponentDescriptorFile: 'component-descriptor.js',
  mainLocalePath: 'src/locales/',
  navigation: {
    items: [
      {
        'id': 'profile-business-card',
        'parentId': 'identity-profile',
        'type': 'TAB',
        'labelKey': 'bsc:content.business-card.label',
        'titleKey': 'bsc:content.business-card.title',
        'icon': 'fa:address-card-o',
        'order': 110,
        'path': '/identity/:entityId/business-card',
        'access': [{'type': 'HAS_ANY_AUTHORITY', 'authorities': ['BUSINESSCARD_ADMIN']}]
      }
    ]
  }
};
