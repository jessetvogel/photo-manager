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

function apiRequestImage(endpoint, callback) {
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

function apiPersons(callback) {
  apiRequest('/persons', callback);
}

function apiPersonsPhotosAmount(ids, start, amount, callback) {
  apiRequest('/persons/pictures?personsId=' + ids + '&start=' + start + '&amount=' + amount, callback);
}

function apiPhoto(id, size, callback) {
  apiRequestImage('/picture/' + id + '?size=' + size, callback);
}

function apiPersonPicture(id, callback) {
  apiRequestImage('/persons/' + id + '/profilepicture', callback);
}
