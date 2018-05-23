$(document).ready(() => {
  // Set timer to check server helath
  status.checkHealth();
  setInterval(status.checkHealth, 5000);
});

const status = {

  // Methods
  checkHealth: () => apiHealth((healthy) => status.updateHealth(healthy)),

  updateHealth: (healthy) => {
    if(healthy)
      $('#health').empty().append($('<span>').addClass('glyphicon glyphicon-signal')).append($('<span>').text('online')).addClass('healthy').removeClass('unhealthy');
    else
      $('#health').empty().append($('<span>').addClass('glyphicon glyphicon-exclamation-sign')).append($('<span>').text('offline')).addClass('unhealthy').removeClass('healthy');
  },

  error: (message) => {
    console.log('[ERROR] ' + message); // TODO: show something nice
  }

};
