$(document).ready(function () {
  // Check server health every now and then
  checkHealthStatus();
  setInterval(checkHealthStatus, 5000);

  // Click events sidebar buttons
  $('#button-people').click(loadPeople);
});

function checkHealthStatus() {
  apiHealth(function (healthy) {
    setHealthStatus(healthy);
  });
}

function setHealthStatus(healthy) {
  if(healthy) {
    $('#health').html('').append($('<span>').addClass('glyphicon glyphicon-signal')).append($('<span>').text('online')).addClass('healthy').removeClass('unhealthy');
  }
  else {
    $('#health').html('').append($('<span>').addClass('	glyphicon glyphicon-exclamation-sign')).append($('<span>').text('offline')).addClass('unhealthy').removeClass('healthy');
  }
}

function loadPeople() {
  // Construct content
  var content = $('<div>').addClass('content-people');
  var peopleSearch = $('<div>').addClass('people-search');
  var peopleTiles = $('<div>').addClass('people-tiles').append($('<div>').addClass('loading'));

  // Search bar
  peopleSearch.append($('<span>').addClass('glyphicon glyphicon-search')).append($('<input>').prop('placeholder', 'search by name').keyup(function () {
    var searchTerm = $(this).val().toLowerCase();
    $('.people-tiles .tile').each(function () {
      if(searchMatch($(this).find('.name').text(), searchTerm))
        $(this).show();
      else
        $(this).hide();
    });
  }));

  // Set content
  content.append(peopleSearch);
  content.append(peopleTiles);
  $('#content').html(content);

  // Get list of people
  apiPeople(function (data) {
    // Clear peopleTiles
    peopleTiles.html('').hide();

    // Create tiles
    for(var i = 0;i < data.length; ++i) {
      // Set some peopleData
      setPeopleData(data[i].id, 'name', data[i].name);
      if(data[i].profilePictureUrn == null) setPeopleData(data[i].id, 'profile-picture', false);

      // Set tile content
      var tile = $('<div>').addClass('tile').append($('<span>').addClass('name').text(data[i].name));
      (function (tile) {
        loadProfilePicture(data[i].id, function (data) {
          tile.css({ backgroundImage: 'url(' + data + ')' });
        });
      })(tile);

      // Click event
      (function (id) {
        tile.click(function () {
          loadPerson(id);
        });
      })(data[i].id);

      // Add to tiles
      peopleTiles.append(tile);
    }

    // Fade in
    peopleTiles.css({ animation: 'fadein 0.5s' }).show();
  });
}

function loadPerson(id) {
  // Construct content
  var content = $('<div>').addClass('content-person');
  var personHeader = $('<div>').addClass('person-header');
  var personPictures = $('<div>').addClass('person-pictures');
  var personPicturesEnd = $('<div>').addClass('person-pictures-end');

  // Person header
  var personProfilePicture = $('<div>').addClass('person-profile-picture');
  personHeader.append(personProfilePicture);
  personHeader.append($('<div>').addClass('person-name').text(getPeopleData(id, 'name')));
  loadProfilePicture(id, function (data) {
    personProfilePicture.css({ backgroundImage: 'url(' + data + ')'});
  });

  // Set interval to check if should load new batch TODO: is there a better way?
  var t = setInterval(function () {
    if($('.content-person').length == 0) {
      clearInterval(t);
      return;
    }
    if(!loadingBatch && checkPersonShouldLoadNewBatch()) {
      loadPersonPicturesBatch(id, PICTURES_PER_BATCH);
    }
  }, 100);

  // Set content
  content.append(personHeader);
  content.append(personPictures);
  content.append(personPicturesEnd);
  $('#content').html(content);

  reachedEndOfPersonFeed = false;
  loadPersonPicturesBatch(id, PICTURES_PER_BATCH);
}

var PICTURES_PER_BATCH = 12;
var loadingBatch = false;
var reachedEndOfPersonFeed = false;

function checkPersonShouldLoadNewBatch() {
  return (!reachedEndOfPersonFeed && $('.person-pictures-end').offset().top < $('body').height());
}

function loadPersonPicturesBatch(id, amount) {
  // Indicate a batch is being loaded
  loadingBatch = true;

  // Add loading icon
  $('.person-pictures-end').append($('<div>').addClass('loading'));

  // Load pictures
  var start = $('.person-pictures .picture').length;
  apiSearch({ people: [ id ] }, start, amount, function (data) {
    // Check if any pictures were returned
    if(data.pictures.length == 0) {
      reachedEndOfPersonFeed = true;
      $('.person-pictures-end').html($('<span>').addClass('text-end-of-feed').text('~'));
      return;
    }

    // Create pictures
    for(var i = 0;i < data.pictures.length; ++i) {
      // Set picture content
      var picture = $('<div>').addClass('picture');

      // Click event
      (function (picture) {
        apiPicture(data.pictures[i].id, 'midi', function (data) {
          picture.css({ backgroundImage: 'url(' + data + ')'});
          picture.click(function () {
            overlay($('<img>').addClass('picture-large').prop('src', data));
          });
        });
      })(picture);

      // Add to pictures
      $('.person-pictures').append(picture);
    }

    // Remove loading icon
    $('.person-pictures-end').html('');

    // Check if a new batch should be loaded
    if(checkPersonShouldLoadNewBatch()) {
      loadPersonPicturesBatch(id, PICTURES_PER_BATCH);
    }
    else {
      // Indicate no batch is being loaded anymore
      loadingBatch = false;
    }
  });
}

function overlay(content) {
  // Create overlay
  var overlay = $('<div>').addClass('overlay').click(function() { $(this).remove(); }).append($('<span>').addClass('glyphicon glyphicon-remove overlay-exit').click(function () { $(this).parent().remove(); })).append(content);

  // Append to body
  $('body').append(overlay);
}

function getPeopleData(id, key) {
  if(peopleData[id] == undefined) peopleData[id] = {};
  if(peopleData[id][key] == undefined) return null;
  return peopleData[id][key];
}

var peopleData = {};

function setPeopleData(id, key, value) {
  if(peopleData[id] == undefined) peopleData[id] = {};
  peopleData[id][key] = value;
}

function loadProfilePicture(id, callback) {
  var profilePictureData = getPeopleData(id, 'profile-picture');

  // In case this person has no picture
  if(profilePictureData == false) {
    callback('/img/profile-picture-default.png');
    return;
  }

  // In case the picture is already loaded
  if(profilePictureData != null) {
    callback(profilePictureData);
    return;
  }

  // Otherwise, load the picture
  else {
    (function (id, callback) {
      apiProfilePicture(id, function (data) {
        setPeopleData(id, 'profile-picture', data);
        callback(data);
      });
    })(id, callback);
  }
}

function searchMatch(string, searchTerm) {
  return simplifyString(string).includes(simplifyString(searchTerm));
}

function simplifyString(str) {
  return str.toLowerCase()
            .replace(/[áàâä]/g, 'a')
            .replace(/[úùûü]/g, 'u')
            .replace(/[éèêë]/g, 'e')
            .replace(/[íìîï]/g, 'i')
            .replace(/[óòôö]/g, 'o')
            .replace(/[^A-Za-z0-9\-_]/g, '-');
}
