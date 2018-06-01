const overlay = {

  // Variables
  currentIndex: -1,

  // Methods
  create: () => {
    if($('.overlay').length > 0) return;

    $('<div>')
      .addClass('overlay')
      .append($('<div>').addClass('overlay-background'))
      .append($('<div>').addClass('overlay-content'))
      .append($('<span>').addClass('glyphicon glyphicon-remove overlay-exit').click(overlay.close))
      .append('<div class="tag"><div class="tag-button"><span class="glyphicon glyphicon-tag"></span></div><div class="tag-input"><div class="tag-input-options"></div><input placeholder="tag person"/></div></div>')
      .appendTo($('body'));

    $('.tag-button').click(() => {
      $('.tag-input').fadeToggle(200);
      $('.tag-input input').focus();
    });

    $('.tag-input input').on('input', function (event) {
      $('.tag-input-options').empty();
      var searchTerm = $(this).val();
      if(searchTerm.length == 0) return;
      var count = 0;
      for(var key in data.data) {
        if(key.startsWith('person')) {
          var name = data.get(key, 'name');
          if(searchMatch(name, searchTerm)) {
            var option = $('<div>').addClass('option').text(name).click(function () {
              $('.selected').removeClass('selected');
              $(this).addClass('selected');
            }).dblclick(function () {
              console.log('Double clicked ' + name);
            });
            if(count == 0) option.addClass('selected');
            $('.tag-input-options').append(option);
            if((++count) >= 3) break;
          }
        }
      }
    }).keydown(function (event) {
      // Up / down arrow keys -> Scroll through options
      var key = event.originalEvent.keyCode;
      if(key == 38 || key == 40) {
        var selected = $('.tag-input-options .option.selected');
        if(selected.length == 0) return;
        var next = (key == 38) ? selected.next() : selected.prev();
        if(next.length == 0) return;
        next.addClass('selected');
        selected.removeClass('selected');
        event.originalEvent.preventDefault();
        return;
      }

      // Escape -> hide tagging
      if(key == 27) {
        $('.tag-button').click();
        return;
      }

      // Enter key -> tag person
      if(key == 13) {
        var selected = $('.tag-input-options .option.selected');
        if(selected.length == 0) return;
        var name = selected.text();
        api.tag(feed.pictures[overlay.currentIndex], [name]);
        $('.tag-input input').val('');
        $('.tag-input-options').empty();
        return;
      }
    });
  },

  show: (index) => {
    // Make sure there is a feed of pictures
    if(!feed.active()) return;

    // Make sure index is valid
    if(index < 0) return;
    if(index >= feed.pictures.length) { // TODO: this should be done in advance (?)
      if(!feed.reachedEnd) feed.loadBatch();
      return;
    }
    overlay.currentIndex = index;

    // Create overlay (if it doesn't exist)
    overlay.create();

    // Set picture as overlay content
    $('.overlay-content').css({ backgroundImage: $('.pictures-feed .picture')[index].style.backgroundImage });
  },

  close: () => $('.overlay').remove(),

  next: () => { if($('.overlay').length != 0) overlay.show(overlay.currentIndex + 1); },
  
  previous: () => { if($('.overlay').length != 0) overlay.show(overlay.currentIndex - 1); },

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
