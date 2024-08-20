function initStatus() {
  // Set timer to check server helath
  status.checkHealth();
  setInterval(status.checkHealth, 5000);
};

const status = {

  // Methods
  checkHealth: () => api.health((healthy) => status.updateHealth(healthy)),

  updateHealth: (healthy) => {
    const health = $('#health');
    if(healthy) {
      setHTML(health, '<span class="signal"></span><span>online</span>');
      addClass(health, 'healthy');
      removeClass(health, 'unhealthy');
    }
    else {
      setHTML(health, '<span class="no-signal"></span><span>offline</span>');
      addClass(health, 'unhealthy');
      removeClass(health, 'healthy');
    }
  },

  error: (message) => {
    console.log('[ERROR] ' + message); // TODO: show something nice
  }

};
