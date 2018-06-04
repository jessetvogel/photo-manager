const overlay = {

  // Variables
  feedIndex: -1,

  // Methods
  open: () => {
    if($('.overlay').length > 0) return;

    $('<div>')
      .addClass('overlay')
      .append($('<div>').addClass('overlay-background'))
      .append($('<div>').addClass('overlay-content'))
      .append($('<span>').addClass('glyphicon glyphicon-remove overlay-exit').click(overlay.close))
      .append($('<div>').addClass('tag-button').append($('<span>').addClass('glyphicon glyphicon-tag')).click(() => { if(focus.current() == overlay) focus.open(tag); else if(focus.current() == tag) focus.close(); }))
      .appendTo($('body'));
  },

  close: () => $('.overlay').remove(),

  show: (index) => {
    // Make sure there is a feed of pictures
    if(!feed.active()) return;

    // Make sure index is valid
    if(index < 0) return;
    if(index >= feed.pictures.length) { // TODO: this should be done in advance (?)
      if(!feed.reachedEnd) feed.loadBatch();
      return;
    }
    overlay.feedIndex = index;

    // Create overlay if it doesn't exist
    if($('.overlay').length == 0) focus.open(overlay);

    // Set picture as overlay content
    $('.overlay-content').css({ backgroundImage: $('.pictures-feed .picture')[index].style.backgroundImage });
  },

  next: () => { if($('.overlay').length != 0) overlay.show(overlay.feedIndex + 1); },
  
  previous: () => { if($('.overlay').length != 0) overlay.show(overlay.feedIndex - 1); },

};
