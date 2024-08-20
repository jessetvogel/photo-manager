const overlay = {

  // Variables
  feedIndex: -1,

  // Methods
  open: () => {
    if($('.overlay') != undefined) return;

    const divOverlay = create('div', '', { 'class': 'overlay' });
    divOverlay.append(create('div', '', { 'class': 'overlay-background' }));
    divOverlay.append(create('div', '', { 'class': 'overlay-content' }));
    const overlayExit = create('span', '', { 'class': 'overlay-exit' });
    onClick(overlayExit, focus.close);
    divOverlay.append(overlayExit);
    const overlayButtons = create('div', '', { 'class': 'overlay-buttons' });
    const downloadButton = create('div', '', { 'class': 'download-button' });
    const tagButton = create('div', '', { 'class': 'tag-button' });
    onClick(downloadButton, () => { window.open(api.url + '/media/' + feed.media[overlay.feedIndex].id, '_blank'); });
    onClick(tagButton, () => { if(focus.current() == overlay) focus.open(tag); else if(focus.current() == tag) focus.close(); });
    overlayButtons.append(downloadButton);
    overlayButtons.append(tagButton);
    divOverlay.append(overlayButtons);
    document.body.append(divOverlay);
  },

  close: () => $('.overlay').remove(),

  show: (index) => {
    // Make sure there is a feed of media
    if(!feed.active()) return;

    // Make sure index is valid
    if(index < 0) return;
    if(index >= feed.media.length) { // TODO: this should be done in advance (?)
      if(!feed.reachedEnd) feed.loadBatch();
      return;
    }
    overlay.feedIndex = index;

    // Create overlay if it doesn't exist
    if($('.overlay') == undefined)
      focus.open(overlay);

    // Set medium as overlay content
    const overlayContent = $('.overlay-content');
    clear(overlayContent);
    overlayContent.style.backgroundImage = 'none';

    const type = feed.media[index].type;
    if(type == 'photo') {
      // overlayContent.style.backgroundImage = document.querySelectorAll('.media-feed > div')[index].style.backgroundImage;
      overlayContent.style.backgroundImage = `url('${api.url}/media/${feed.media[index].id}')`;
    }
    
    else if(type == 'video') {
      overlayContent.append(create('video', '<source src="http://localhost:4321/media/' + feed.media[index].id + '" type="video/mp4"></source>', { 'controls': 'true', 'loop': 'true' }));
    }

    else {
      console.log('dont know how to show of type ' + type);
    }
  },

  next: () => { if($('.overlay') != undefined) overlay.show(overlay.feedIndex + 1); },
  
  previous: () => { if($('.overlay') != undefined) overlay.show(overlay.feedIndex - 1); },

};
