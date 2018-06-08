const api = {
  // url = 'https://www.robertvankammen.nl:9090';
  url: 'http://' + window.location.hostname + ':4321',

  request: (endpoint, callback) => {
    $.ajax({
      url: api.url + endpoint,
      // headers: {
      //   'Authorization': 'Basic cm9iZXJ0OnRlc3Q='
      // }
    })
    .done(callback)
    .fail(api.error);

    console.log('[API request] ' + api.url + endpoint);
  },

  requestPicture: (endpoint, callback) => {
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

  health: (callback) => api.request('/health', callback),

  people: (callback) => api.request('/people', callback),

  albums: (callback) => api.request('/albums', callback),
  
  search: (filters, start, amount, callback) => {
    var filterTerms = '';
    for(var x in filters)
      filterTerms += '&' + x + '=' + filters[x].join(';');
    api.request('/search?start=' + start + '&amount=' + amount + filterTerms, callback);
  },

  picture: (id, size, callback) => api.requestPicture('/pictures/' + id + '?size=' + size, callback),

  profilePicture: (id, callback) => api.requestPicture('/people/' + id + '/profilepicture', callback),

  coverPicture: (id, callback) => api.requestPicture('/albums/' + id + '/cover', callback),

  tag: (id, names, callback) => api.request('/pictures/' + id + '/tag?names=' + names.join(','), callback),
  
  untag: (id, names, callback) => api.request('/pictures/' + id + '/untag?names=' + names.join(','), callback),

};
