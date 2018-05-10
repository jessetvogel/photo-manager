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

  // Get list of persons
  apiPersons(function (data) {
    // Clear peopleTiles
    peopleTiles.html('').hide();

    // Create tiles
    for(var i = 0;i < data.length; ++i) {
      // Set some peopleData
      setPeopleData(data[i].id, 'name', data[i].name);
      if(data[i].profilePictureUrn == null) setPeopleData(data[i].id, 'picture', false);

      // Set tile content
      var tile = $('<div>').addClass('tile').append($('<span>').addClass('name').text(data[i].name));
      (function (tile) {
        loadPersonPicture(data[i].id, function (data) {
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
  var personPhotos = $('<div>').addClass('person-photos');
  var personPhotosEnd = $('<div>').addClass('person-photos-end');

  // Person header
  var personPicture = $('<div>').addClass('person-picture');
  personHeader.append(personPicture);
  personHeader.append($('<div>').addClass('person-name').text(getPeopleData(id, 'name')));
  loadPersonPicture(id, function (data) {
    personPicture.css({ backgroundImage: 'url(' + data + ')'});
  });

  // Set interval to check if should load new batch TODO: is there a better way?
  var t = setInterval(function () {
    if($('.content-person').length == 0) {
      clearInterval(t);
      return;
    }
    if(!loadingBatch && checkPersonShouldLoadNewBatch()) {
      loadPersonPhotoBatch(id, PHOTOS_PER_BATCH);
    }
  }, 100);

  // Set content
  content.append(personHeader);
  content.append(personPhotos);
  content.append(personPhotosEnd);
  $('#content').html(content);

  reachedEndOfPersonFeed = false;
  loadPersonPhotoBatch(id, PHOTOS_PER_BATCH);
}

var PHOTOS_PER_BATCH = 12;
var loadingBatch = false;
var reachedEndOfPersonFeed = false;

function checkPersonShouldLoadNewBatch() {
  return (!reachedEndOfPersonFeed && $('.person-photos-end').offset().top < $('body').height());
}

function loadPersonPhotoBatch(id, amount) {
  // Indicate a batch is being loaded
  loadingBatch = true;

  // Add loading icon
  $('.person-photos-end').append($('<div>').addClass('loading'));

  // Load photos
  var start = $('.person-photos .photo').length;
  apiPersonsPhotosAmount(id, start, amount, function (data) {
    // Check if any photos were returned
    if(data.pictures.length == 0) {
      reachedEndOfPersonFeed = true;
      $('.person-photos-end').html($('<span>').addClass('text-end-of-feed').text('~'));
      return;
    }

    // Create photos
    for(var i = 0;i < data.pictures.length; ++i) {
      // Set photo content
      var photo = $('<div>').addClass('photo');

      // Click event
      (function (photo) {
        apiPhoto(data.pictures[i].id, 'midi', function (data) {
          photo.css({ backgroundImage: 'url(' + data + ')'});
          photo.click(function () {
            overlay($('<img>').addClass('photo-large').prop('src', data));
          });
        });
      })(photo);

      // Add to photos
      $('.person-photos').append(photo);
    }

    // Remove loading icon
    $('.person-photos-end').html('');

    // Check if a new batch should be loaded
    if(checkPersonShouldLoadNewBatch()) {
      loadPersonPhotoBatch(id, PHOTOS_PER_BATCH);
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

function loadPersonPicture(id, callback) {
  var pictureData = getPeopleData(id, 'picture');

  // In case this person has no picture
  if(pictureData == false) {
    callback('/img/person-default.png');
    return;
  }

  // In case the picture is already loaded
  if(pictureData != null) {
    callback(pictureData);
    return;
  }

  // Otherwise, load the picture
  else {
    (function (id, callback) {
      apiPersonPicture(id, function (data) {
        setPeopleData(id, 'picture', data);
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
