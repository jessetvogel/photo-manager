const feed = {

	// Constants
	PICTURES_PER_BATCH: 12,
	FEED_REFRESH_TIME: 100,

	// Variables
	currentFilters: null,
	loadingBatch: false,
	reachedEnd: false,
	interval: null,

	// Methods
	start: (filters) => {
		// Set current filters
		feed.currentFilters = filters;

		// Set interval to check if should load new batch
		feed.interval = setInterval(() => {
			// Stop when feed disappears
			if($('.pictures-feed').length == 0) {
					clearInterval(feed.interval);
					return;
			}

			// Load batch when necessary
			if(!feed.loadingBatch && feed.shouldUpdate())
				feed.loadBatch();
		 }, feed.FEED_REFRESH_TIME);

		// Initially, clear the feed, indicate we have not reached the end, are not loading a batch, and load the first batch
		$('.pictures-feed').empty();
		feed.reachedEnd = false;
		feed.loadingBatch = false;
		feed.loadBatch();
	},

	shouldUpdate: () => (!feed.reachedEnd && $('.pictures-feed-end').offset().top < $('body').height()),

	loadBatch: () => {
		// Make sure we don't load a batch if already busy loading one
		if(feed.loadingBatch) return;
		feed.loadingBatch = true;

		// Add loading icon
		$('.pictures-feed-end').append($('<div>').addClass('loading'));

		// Load pictures
		var start = $('.pictures-feed .picture').length;
		apiSearch(feed.currentFilters, start, feed.PICTURES_PER_BATCH, (data) => {
			// If no pictures were returned, we reached the end of the stream
			if(data.pictures.length == 0) {
				feed.reachedEnd = true;
				$('.pictures-feed-end').html($('<span>').addClass('text-end-of-feed').text('~'));
				return;
			}

			// Add pictures to feed
			for(var i = 0;i < data.pictures.length; ++i) {
				// Set picture content
				var picture = $('<div>').addClass('picture');
				((picture) => apiPicture(data.pictures[i].id, 'small', (data) => {
					picture.css({ backgroundImage: 'url(' + data + ')'});
					picture.click(() => overlay.show($('.pictures-feed .picture').index(picture)));
				}))(picture);
				$('.pictures-feed').append(picture);
			}

			// Remove loading icon
			$('.pictures-feed-end').empty();

			// Load new batch if necessary, otherwise indicate no batch is being loaded anymore
			if(feed.shouldUpdate())
				feed.loadBatch();
			else
				feed.loadingBatch = false;
		});
	}

};
