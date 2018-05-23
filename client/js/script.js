function loadPeople() {
  // Construct content
  var content = $('<div>').addClass('content-people');
  var peopleSearch = $('<div>').addClass('people-search');
  var peopleTiles = $('<div>').addClass('people-tiles').append($('<div>').addClass('loading'));

  // Search bar
  peopleSearch.append($('<span>').addClass('glyphicon glyphicon-search')).append($('<input>').prop('placeholder', 'search by name').keyup(() => {
    var searchTerm = $(this).val().toLowerCase();
    $('.people-tiles .tile').each(() => {
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
  apiPeople((_data) => {
    // Clear peopleTiles
    peopleTiles.empty().hide();

    // Create tiles
    for(var i = 0;i < _data.length; ++i) {
      // Set some people data
      data.set('person' + _data[i].id, 'name', _data[i].name);
      data.set('person' + _data[i].id, 'profilePicture', _data[i].profilePicture);

      // Set tile content
      var tile = $('<div>').addClass('tile').append($('<span>').addClass('name').text(_data[i].name));
      ((tile) => loadProfilePicture(_data[i].id, (data) => tile.css({ backgroundImage: 'url(' + data + ')' })))(tile);

      // Click event
      ((id) => tile.click(() => setContentPerson(id)))(_data[i].id);

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
  albumsSearch.append($('<span>').addClass('glyphicon glyphicon-search')).append($('<input>').prop('placeholder', 'search by title').keyup(() => {
    var searchTerm = $(this).val().toLowerCase();
    $('.albums-tiles .tile').each(() => {
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
  apiAlbums((_data) => {
    // Clear peopleTiles
    albumsTiles.empty().hide();

    // Create tiles
    for(var i = 0;i < _data.length; ++i) {
      // Set some album data
      data.set('album' + _data[i].id, 'title', _data[i].title);
      data.set('album' + _data[i].id, 'coverPicture', null);

      // Set tile content
      var tile = $('<div>').addClass('tile').append($('<span>').addClass('title').text(_data[i].title));
      ((tile) => loadCoverPicture(_data[i].id, (data) => tile.css({ backgroundImage: 'url(' + data + ')' })))(tile);

      // Click event
      ((id) => tile.click(() => setContentAlbum(id)))(_data[i].id);

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
      apiProfilePicture(id, (_data) => {
        data.set('person' + id, 'profilePicture', _data);
        callback(_data);
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
      apiCoverPicture(id, (_data) => {
        data.set('album' + id, 'coverPicture', _data);
        callback(_data);
      });
    })(id, callback);
}

// * UTIL *
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
