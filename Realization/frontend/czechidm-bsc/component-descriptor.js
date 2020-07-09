module.exports = {
  id: 'bsc',
  name: 'Bsc',
  description: 'Components for Bsc module',
  components: [
    {
      id: 'button-form-value',
      type: 'form-attribute-renderer',
      persistentType: 'SHORTTEXT',
      faceType: 'BSC-BUTTON',
      component: require('./src/components/ButotnFormAttributeRenderer'),
      labelKey: 'Button'
    }
  ]
};
