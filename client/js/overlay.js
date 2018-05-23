const overlay = {

  // Variables
  currentIndex: -1,

  // Methods
  show: (index) => {
    // Make sure there is a feed of pictures
    var list = $('.pictures-feed .picture');
    if(list.length == 0) return;

    // Make sure index is valid
    if(index < 0) return;
    if(index >= list.length) {
      if(!feed.reachedEnd) feed.loadBatch();
      return;
    }
    overlay.currentIndex = index;

    // Set picture as overlay content
    overlay.setContent($('<div>').addClass('picture').css({ backgroundImage: list[index].style.backgroundImage }));
  },

  close: () => $('.overlay').remove(),

  next: () => { if($('.overlay').length != 0) overlay.show(overlay.currentIndex + 1); },
  
  previous: () => { if($('.overlay').length != 0) overlay.show(overlay.currentIndex - 1); },

  setContent: (content) => {
    // Create overlay if it does not exist yet, otherwise empty it
    var div = $('.overlay');
    if(div.length == 0) {
      div = $('<div>').addClass('overlay').click(overlay.close);
      $('body').append(div); 
    }
    else {
      div.empty();
    }

    // Set content
    div.append($('<span>').addClass('glyphicon glyphicon-remove overlay-exit').click(overlay.close)).append(content);
  }

};


// Use left and right arrow keys to navigate between pictures
window.onkeydown = (event) => {
  switch(event.keyCode ? event.keyCode : event.which) {
    case 37:
      overlay.previous();
      break;

    case 39:
      overlay.next();
      break;
  }
};
