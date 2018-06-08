$(document).ready(() => {
  // Set click event listeners
  $('#button-albums').click(loadAlbums);
  $('#button-people').click(loadPeople);
  $('#button-settings').click(loadSettings);

  // Load people and albums already once
  data.updatePeople();
  data.updateAlbums();
});

function loadPeople() {
  // Construct content
  var content = $('<div>').addClass('content-people');
  var peopleSearch = $('<div>').addClass('people-search');
  var peopleTiles = $('<div>').addClass('people-tiles').append($('<div>').addClass('loading'));

  // Search bar
  peopleSearch.append($('<span>').addClass('glyphicon glyphicon-search')).append($('<input>').prop('placeholder', 'search by name').on('input', function () {
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

  data.updatePeople(() => {
    // Clear peopleTiles
    peopleTiles.empty().hide();

    // Create a tile for each person
    for(var key in data.data) {
      if(!key.startsWith('person')) continue;
      var person = data.data[key];
      var personId = key.substring(6); // TODO

      // Set tile content
      var tile = $('<div>').addClass('tile').append($('<span>').addClass('name').text(person.name));
      ((tile) => loadProfilePicture(personId, (data) => tile.css({ backgroundImage: 'url(' + data + ')' })))(tile);

      // Click event
      ((personId) => tile.click(() => setContentPerson(personId)))(personId);

      // Add to tiles
      peopleTiles.append(tile);
    }

    // Fade in
    peopleTiles.css({ animation: 'fadein 0.5s' }).show();
  });
}

function loadAlbums() {
  // Construct content
  var content = $('<div>').addClass('content-albums');
  var albumsSearch = $('<div>').addClass('albums-search');
  var albumsTiles = $('<div>').addClass('albums-tiles').append($('<div>').addClass('loading'));

  // Search bar
  albumsSearch.append($('<span>').addClass('glyphicon glyphicon-search')).append($('<input>').prop('placeholder', 'search by title').on('input', function () {
    var searchTerm = $(this).val().toLowerCase();
    $('.albums-tiles .tile').each(function () {
      if(searchMatch($(this).find('.title').text(), searchTerm))
        $(this).show();
      else
        $(this).hide();
    });
  }));

  // Set content
  content.append(albumsSearch);
  content.append(albumsTiles);
  $('#content').html(content);

  // Get list of albums
  data.updateAlbums(() => {
    // Clear albumsTiles
    albumsTiles.empty().hide();

    // Create a tile for each album
    for(var key in data.data) {
      if(!key.startsWith('album')) continue;
      var album = data.data[key];
      var albumId = key.substring(5); // TODO

      // Set tile content
      var tile = $('<div>').addClass('tile').append($('<span>').addClass('title').text(album.title));
      ((tile) => loadCoverPicture(albumId, (data) => tile.css({ backgroundImage: 'url(' + data + ')' })))(tile);

      // Click event
      ((albumId) => tile.click(() => setContentAlbum(albumId)))(albumId);

      // Add to tiles
      albumsTiles.append(tile);
    }

    // Fade in
    albumsTiles.css({ animation: 'fadein 0.5s' }).show();
  });
}

function setContentPerson(personId) {
  // Construct content
  var content = $('<div>').addClass('content-person');
  var personHeader = $('<div>').addClass('person-header');
  var picturesList = $('<div>').addClass('pictures-feed');
  var picturesListEnd = $('<div>').addClass('pictures-feed-end');

  // Person header
  var profilePicture = $('<div>').addClass('person-profile-picture');
  personHeader.append(profilePicture);
  personHeader.append($('<div>').addClass('person-name').text(data.get('person' + personId, 'name')));
  loadProfilePicture(personId, (data) => profilePicture.css({ backgroundImage: 'url(' + data + ')'}));

  // Set content
  content.append(personHeader);
  content.append(picturesList);
  content.append(picturesListEnd);
  $('#content').html(content);

  // Start feed
  feed.start({ people: [personId] });
}

function setContentAlbum(albumId) {
  // Construct content
  var content = $('<div>').addClass('content-album');
  var albumHeader = $('<div>').addClass('album-header');
  var picturesList = $('<div>').addClass('pictures-feed');
  var picturesListEnd = $('<div>').addClass('pictures-feed-end');

  // Album header
  albumHeader.append($('<div>').addClass('album-title').text(data.get('album' + albumId, 'title')));

  // Set content
  content.append(albumHeader);
  content.append(picturesList);
  content.append(picturesListEnd);
  $('#content').html(content);

  // Start feed
  feed.start({ albums: [albumId] });
}

function loadProfilePicture(id, callback) {
  var profilePictureData = data.get('person' + id, 'profilePicture');

  // In case this person has no picture
  if(profilePictureData == false) {
    // callback('/img/profile-picture-default.png');
    return;
  }

  // In case the picture is already loaded
  if(profilePictureData != true) {
    callback(profilePictureData);
    return;
  }

  // Otherwise, load the picture
  else {
    (function (id, callback) {
      api.profilePicture(id, (response) => {
        data.set('person' + id, 'profilePicture', response);
        callback(response);
      });
    })(id, callback);
  }
}

function loadCoverPicture(id, callback) {
  var coverPictureData = data.get('album' + id, 'coverPicture');

  // In case the picture is already loaded
  if(coverPictureData != null) {
    callback(coverPictureData);
    return;
  }

  // Otherwise, load the picture
  (function (id, callback) {
      api.coverPicture(id, (response) => {
        data.set('album' + id, 'coverPicture', response);
        callback(response);
      });
    })(id, callback);
}

function loadSettings() {
  $('#content').empty().append("Settings TODO");
}