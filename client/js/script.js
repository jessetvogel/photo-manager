function init() {
  initStatus();

  // Set click event listeners
  onClick($('#button-albums'), loadAlbums);
  onClick($('#button-people'), loadPeople);
  // $('#button-settings').click(loadSettings);

  // Load people and albums already once
  data.updatePeople();
  data.updateAlbums();
};

function loadPeople() {
  // Construct content
  const content = $('#content');
  const contentPeople = create('div', '', { 'class': 'content-people' });
  const peopleSearch = create('div', '', { 'class': 'people-search' });
  const peopleTiles = create('div', '<div class="loading"></div>', { 'class': 'people-tiles' });

  // Search bar
  peopleSearch.append(create('span', '', { 'class': 'glyphicon glyphicon-search' }));
  const peopleSearchInput = create('input', '', { 'placeholder': 'search by name' });
  onInput(peopleSearchInput, function () {
    const searchTerm = peopleSearchInput.value.toLowerCase();
    for(var tile of document.querySelectorAll('.people-tiles .tile')) {
      if(searchMatch(tile.querySelector('.name').innerText, searchTerm))
        tile.style.display = 'block';
      else
        tile.style.display = 'none';
    };
  });
  peopleSearch.append(peopleSearchInput);

  // Set content
  contentPeople.append(peopleSearch);
  contentPeople.append(peopleTiles);
  clear(content);
  content.append(contentPeople);

  data.updatePeople(() => {
    // Clear peopleTiles
    clear(peopleTiles);
    peopleTiles.style.display = 'none';

    // Create a tile for each person
    for(var key in data.data) {
      if(!key.startsWith('person')) continue;

      const person = data.data[key];
      const personId = key.substring(6); // TODO

      // Set tile content
      const tile = create('div', `<span class="name">${person.name}</span>`, { 'class': 'tile' });
      loadProfilePicture(personId, (data) => tile.style.backgroundImage = 'url(' + data + ')');

      // Click event
      onClick(tile, () => setContentPerson(personId));
    
      // Add to tiles
      peopleTiles.append(tile);
    }

    // Fade in
    peopleTiles.style.animation = 'fadein 0.5s';
    peopleTiles.style.display = 'flex';
  });
}

function loadAlbums() {
  // Construct content
  const contentAlbums = create('div', '', { 'class': 'content-albums' });
  const albumsSearch = create('div', '', { 'class': 'albums-search' });
  const albumsTiles = create('div', '<div class="loading"></div>', { 'class': 'albums-tiles' });

  // Search bar
  albumsSearch.append(create('span', '', { 'class': 'glyphicon glyphicon-search' }));
  const albumSearchInput = create('input', '', { 'placeholder': 'search by title' });
  onInput(albumSearchInput, () => {
    const searchTerm = albumSearchInput.value.toLowerCase();
    for(var tile of document.querySelectorAll('.albums-tiles .tile')) {
      if(searchMatch(tile.querySelector('.title').innerText, searchTerm))
        tile.style.display = 'flex';
      else
        tile.style.display = 'none';
    };
  });
  albumsSearch.append(albumSearchInput);

  // Set content
  contentAlbums.append(albumsSearch);
  contentAlbums.append(albumsTiles);
  const content = $('#content');
  clear(content);
  content.append(contentAlbums);

  // Get list of albums
  data.updateAlbums(() => {
    // Clear albumsTiles
    clear(albumsTiles);
    albumsTiles.style.display = 'none';

    // Create a tile for each album
    for(var key in data.data) {
      if(!key.startsWith('album')) continue;
      const album = data.data[key];
      const albumId = key.substring(5); // TODO

      // Set tile content
      const tile = create('div', `<span class="title">${album.title}</span>`, { 'class': 'tile' });
      loadCoverPicture(albumId, (data) => tile.style.backgroundImage = 'url(' + data + ')');

      // Click event
      onClick(tile, () => setContentAlbum(albumId));

      // Add to tiles
      albumsTiles.append(tile);
    }

    // Fade in
    albumsTiles.style.animation = 'fadein 0.5s';
    albumsTiles.style.display = 'flex';
  });
}

function setContentPerson(personId) {
  // Construct content
  const content = $('#content');
  const contentPerson = create('div', '', { 'class': 'content-person' });
  const personHeader = create('div', '', { 'class': 'person-header' });
  const mediaFeed = create('div', '', { 'class': 'media-feed' });
  const mediaFeedEnd = create('div', '', { 'class': 'media-feed-end' });

  // Person header
  const profilePicture = create('div', '', { 'class': 'person-profile-picture' });
  personHeader.append(profilePicture);
  personHeader.append(create('div', data.get('person' + personId, 'name'), { 'class': 'person-name' }));
  loadProfilePicture(personId, (data) => profilePicture.style.backgroundImage = 'url(' + data + ')');

  // Set content
  contentPerson.append(personHeader);
  contentPerson.append(mediaFeed);
  contentPerson.append(mediaFeedEnd);
  clear(content);
  content.append(contentPerson);

  // Start feed
  feed.start({ people: [personId] });
}

function setContentAlbum(albumId) {
  // Construct content
  const content = $('#content');
  const contentAlbum = create('div', '', { 'class': 'content-album' });
  const albumHeader = create('div', '', { 'class': 'album-header' });
  const mediaFeed = create('div', '', { 'class': 'media-feed' });
  const mediaFeedEnd = create('div', '', { 'class': 'media-feed-end' });

  // Album header
  albumHeader.append(create('div', data.get('album' + albumId, 'title'), { 'class': 'album-title' }));

  // Set content
  contentAlbum.append(albumHeader);
  contentAlbum.append(mediaFeed);
  contentAlbum.append(mediaFeedEnd);
  clear(content);
  content.append(contentAlbum);

  // Start feed
  feed.start({ albums: [albumId] });
}

function loadProfilePicture(id, callback) {
  var profilePictureData = data.get('person' + id, 'picture');

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
        data.set('person' + id, 'picture', response);
        callback(response);
      });
    })(id, callback);
  }
}

function loadCoverPicture(id, callback) {
  // In case the picture is already loaded
  const coverPictureData = data.get('album' + id, 'cover');
  if(coverPictureData != null) {
    callback(coverPictureData);
    return;
  }

  // Otherwise, load the picture
  (function (id, callback) {
      api.albumCover(id, (response) => {
        data.set('album' + id, 'cover', response);
        callback(response);
      });
    })(id, callback);
}

function loadSettings() {
  $('#content').empty().append("Settings TODO");
}
