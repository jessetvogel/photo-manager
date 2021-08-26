const api = {
  url: 'http://' + window.location.hostname + ':4321',

  requestJSON: (endpoint, callback) => {
    var xhr = new XMLHttpRequest();
    xhr.responseType = 'json';
    xhr.onload = function() {
      if(callback != undefined)
        callback(xhr.response);
    };
    xhr.open('GET', api.url + endpoint);
    xhr.send();

    console.log('[API request] ' + api.url + endpoint);
  },

  requestBlob: (endpoint, callback) => {
    var xhr = new XMLHttpRequest();
    xhr.onload = function() {
      var reader = new FileReader();
      reader.onloadend = function() {
        callback(reader.result);
      }
      reader.readAsDataURL(xhr.response);
    };
    xhr.open('GET', api.url + endpoint);
    // xhr.setRequestHeader('Authorization', 'Basic cm9iZXJ0OnRlc3Q=');
    xhr.responseType = 'blob';
    xhr.send();
  },

  error: (data) => status.updateHealth(false),

  health: (callback) => api.requestJSON('/health', callback),

  people: (callback) => api.requestJSON('/people', callback),

  albums: (callback) => api.requestJSON('/albums', callback),
  
  search: (filters, start, amount, callback) => {
    var filterTerms = '';
    for(var x in filters)
      filterTerms += '&' + x + '=' + filters[x].join(';');
    api.requestJSON('/search?start=' + start + '&amount=' + amount + filterTerms, callback);
  },

  media: (id, size, callback) => api.requestBlob('/media/' + id + '?size=' + size, callback),

  profilePicture: (id, callback) => api.requestBlob('/people/' + id + '/picture', callback),

  albumCover: (id, callback) => api.requestBlob('/albums/' + id + '/cover', callback),

  tag: (id, names, callback) => api.requestJSON('/media/' + id + '/tag?names=' + names.join(','), callback),
  
  untag: (id, names, callback) => api.requestJSON('/media/' + id + '/untag?names=' + names.join(','), callback),

};
