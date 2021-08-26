const tag = {

  // Variables
  options: [],

	// Methods
	open: () => {
		if($('.tag') != undefined) return;

    document.body.append(create('div', '<div class="tag-options"></div><div class="tag-input"><input placeholder="tag person"/></div><div class="tagged-people"></div>', { 'class': 'tag' }));
		// $('.tag').hide().fadeIn(100);
    const tagInput = $('.tag-input input');
    onInput(tagInput, tag.refreshOptions);
    onKeyDown(tagInput, (event) => {
	  	const key = event.keyCode;

      // Up / down arrow keys -> Scroll through options
      if(key == 38 || key == 40) {
        const selected = $('.tag-options .option.selected');
        if(selected == undefined) return;
        const next = (key == 38) ? selected.nextSibling : selected.previousSibling;
        if(next == null) return;
        addClass(next, 'selected');
        removeClass(selected, 'selected');
        event.preventDefault();
        return;
      }

      // Enter key -> tag person
      if(key == 13) {
        const selected = $('.tag-options .option.selected');
        if(selected == undefined) return;
        tag.tag(selected.innerText);
        tagInput.value = '';
        clear($('.tag-options'));
        return;
      }

      // Control -> new person option
      if(key == 17) {
      	const name = tagInput.value;
      	if(name.length > 0) {
          clear($('.tag-options'));
          $('.tag-options').append(create('div', name, { 'class': 'option new selected' }));
        }
      }
    })
    onKeyUp(tagInput, (event) => {
      const key = event.keyCode;

	  	// Control -> new person option
	  	if(key == 17) tag.refreshOptions();
	  })

    tagInput.focus();
    tag.refreshTaggedPeople();
	},

	close: () => {
		// $('.tag').fadeOut(100, function () { $(this).remove(); });
    const tag = $('.tag');
    if(tag != undefined) {
      tag.remove();
    }
	},

	refreshOptions: (event) => {
		clear($('.tag-options'));
    const searchTerm = $('.tag-input input').value;
    if(searchTerm.length == 0) return;

    var count = 0;
    for(var i = 0;i < tag.options.length; ++i) {
      if(feed.media[overlay.feedIndex].tagged.includes(tag.options[i])) continue;
      if(searchMatch(tag.options[i], searchTerm)) {
        const option = create('div', tag.options[i], { 'class': 'option' });
        onClick(option, () => {
          $('.selected').removeClass('selected');
          $(this).addClass('selected');
        });
        onDblClick(option, function () {
          console.log('Double clicked ' + tag.options[i]); // ???
        });
        $('.tag-options').append(option);
        if((++count) >= 3) break;
      }
    }

    const firstChild = $('.tag-options').firstChild;
    if(firstChild != undefined)
      addClass(firstChild, 'selected');
	},

  refreshTaggedPeople: () => {
    const tagged = feed.media[overlay.feedIndex].tagged;
    const taggedPeople = $('.tagged-people');
    clear(taggedPeople);
    for(var i = 0;i < tagged.length; ++i) {
        const person = tagged[i];
        const div = create('div', `<span>${person}</span>`, { 'class': 'person' });
        const remove = create('span', '', { 'class': 'glyphicon glyphicon-remove' });
        onClick(remove, () => tag.untag(person));
        div.append(remove);
        taggedPeople.append(div);
    }
  },

  tag: (name) => {
    var currentPicture = feed.media[overlay.feedIndex];
    api.tag(currentPicture.id, [ name ]);
    currentPicture.tagged.push(name);
    tag.refreshTaggedPeople();
    tag.addOption(name);
  },

  untag: (name) => {
    var currentPicture = feed.media[overlay.feedIndex];
    api.untag(currentPicture.id, [ name ]);
    currentPicture.tagged = currentPicture.tagged.filter(e => e !== name);
    tag.refreshTaggedPeople();
  },

  addOption: (name) => {
    if(!tag.options.includes(name))
      tag.options.push(name);
  }

};
