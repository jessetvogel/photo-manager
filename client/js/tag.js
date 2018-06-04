const tag = {

  // Variables
  options: [],

	// Methods
	open: () => {
		if($('.tag').length > 0) return;

		$('<div>').addClass('tag').append($('<div class="tag-options"></div><div class="tag-input"><input placeholder="tag person"/></div><div class="tagged-people"></div>')).appendTo($('body'));
		$('.tag').hide().fadeIn(100);
	  $('.tag-input input')
	  	.on('input', tag.refreshOptions)
	  	.keydown(function (event) {
	  	var key = event.originalEvent.keyCode;

      // Up / down arrow keys -> Scroll through options
      if(key == 38 || key == 40) {
        var selected = $('.tag-options .option.selected');
        if(selected.length == 0) return;
        var next = (key == 38) ? selected.next() : selected.prev();
        if(next.length == 0) return;
        next.addClass('selected');
        selected.removeClass('selected');
        event.originalEvent.preventDefault();
        return;
      }

      // Enter key -> tag person
      if(key == 13) {
        var selected = $('.tag-options .option.selected');
        if(selected.length == 0) return;
        tag.tag(selected.text());
        $('.tag-input input').val('');
        $('.tag-options').empty();
        return;
      }

      // Control -> new person option
      if(key == 17) {
      	var name = $('.tag-input input').val();
      	if(name.length > 0)
      		$('.tag-options').empty().append($('<div>').addClass('option new selected').text(name));
      }
    })
	  .keyup(function (event) {
	  	var key = event.originalEvent.keyCode;

	  	// Control -> new person option
	  	if(key == 17) {
	  		tag.refreshOptions();
	  	}
	  })
  	.focus();

    tag.refreshTaggedPeople();
	},

	close: () => {
		$('.tag').fadeOut(100, function () { $(this).remove(); });
	},

	refreshOptions: (event) => {
		$('.tag-options').empty();
    var searchTerm = $('.tag-input input').val();
    if(searchTerm.length == 0) return;
    var count = 0;
    for(var i = 0;i < tag.options.length; ++i) {
      if(feed.pictures[overlay.feedIndex].tagged.includes(tag.options[i])) continue;
      if(searchMatch(tag.options[i], searchTerm)) {
        var option = $('<div>').addClass('option').text(tag.options[i]).click(function () {
          $('.selected').removeClass('selected');
          $(this).addClass('selected');
        }).dblclick(function () {
          console.log('Double clicked ' + tag.options[i]);
        });
        $('.tag-options').append(option);
        if((++count) >= 3) break;
      }
    }
    $('.tag-options .option').first().addClass('selected');
	},

  refreshTaggedPeople: () => {
    var tagged = feed.pictures[overlay.feedIndex].tagged;
    var taggedPeople = $('.tagged-people');
    taggedPeople.empty();
    for(var i = 0;i < tagged.length; ++i) {
      ((i) => taggedPeople.append($('<div>').addClass('person').append($('<span>').text(tagged[i])).append($('<span>').addClass('glyphicon glyphicon-remove').click(() => tag.untag(tagged[i])))))(i);
    }
  },

  tag: (name) => {
    var currentPicture = feed.pictures[overlay.feedIndex];
    api.tag(currentPicture.id, [ name ]);
    currentPicture.tagged.push(name);
    tag.refreshTaggedPeople();
    tag.addOption(name);
  },

  untag: (name) => {
    var currentPicture = feed.pictures[overlay.feedIndex];
    api.untag(currentPicture.id, [ name ]);
    currentPicture.tagged = currentPicture.tagged.filter(e => e !== name);
    tag.refreshTaggedPeople();
  },

  addOption: (name) => {
    if(!tag.options.includes(name))
      tag.options.push(name);
  }

};
