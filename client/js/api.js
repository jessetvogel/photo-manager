// var apiUrl = 'https://www.robertvankammen.nl:9090';
var apiUrl = 'http://localhost:4321';

function apiRequest(endpoint, callback) {
  $.ajax({
    url: apiUrl + endpoint,
    headers: {
      'Authorization': 'Basic cm9iZXJ0OnRlc3Q='
    }
  })
  .done(callback)
  .fail(apiError);

  console.log('[API request] ' + apiUrl + endpoint);
}

function apiRequestPicture(endpoint, callback) {
  var xhr = new XMLHttpRequest();
  xhr.onload = function() {
    var reader = new FileReader();
    reader.onloadend = function() {
      callback(reader.result);
    }
    reader.readAsDataURL(xhr.response);
  };
  xhr.open('GET', apiUrl + endpoint);
  xhr.setRequestHeader('Authorization', 'Basic cm9iZXJ0OnRlc3Q=');
  xhr.responseType = 'blob';
  xhr.send();
}

function apiError(data) {
  setHealthStatus(false);
}

function apiHealth(callback) {
  apiRequest('/health', callback);
}

function apiPeople(callback) {
  apiRequest('/people', callback);
}

function apiAlbums(callback) {
  apiRequest('/albums', callback);
}

function apiSearch(filters, start, amount, callback) {
  var filterTerms = '';
  for(var x in filters)
    filterTerms += '&' + x + '=' + filters[x].join(';');
  apiRequest('/search?start=' + start + '&amount=' + amount + filterTerms, callback);
}

function apiPicture(id, size, callback) {
  apiRequestPicture('/pictures/' + id + '?size=' + size, callback);
}

function apiProfilePicture(id, callback) {
  apiRequestPicture('/people/' + id + '/profilepicture', callback);
}
