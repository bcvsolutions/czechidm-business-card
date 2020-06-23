module.exports = {
  module: 'bsc',
  childRoutes: [
    {
      path: 'identity/:entityId/',
      component: require('czechidm-core/src/content/identity/Identity'),
      childRoutes: [
        {
          path: 'business-card',
          component: require('./src/content/identities/BscIdentityBusinessCard'),
          access: [{'type': 'HAS_ANY_AUTHORITY', 'authorities': ['BUSINESSCARD_ADMIN']}]
        }
      ]
    }
  ]
};
