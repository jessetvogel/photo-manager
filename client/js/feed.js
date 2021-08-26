const feed = {
	// Constants
	MEDIA_PER_BATCH: 12,
	FEED_REFRESH_TIME: 100,

	// Variables
	currentFilters: null,
	loadingBatch: false,
	reachedEnd: false,
	interval: null,
	media: [],

	// Methods
	start: (filters) => {
		// Set current filters
		feed.currentFilters = filters;

		// Set interval to check if should load new batch
		feed.interval = setInterval(() => {
			// Stop when feed disappears
			if($('.media-feed') == undefined) {
				clearInterval(feed.interval);
				feed.interval = null;
				return;
			}

			// Load batch when necessary
			if(!feed.loadingBatch && feed.shouldUpdate())
				feed.loadBatch();
		 }, feed.FEED_REFRESH_TIME);

		// Initially, clear the feed, indicate we have not reached the end, are not loading a batch, and load the first batch
		clear($('.media-feed'));
		feed.media = [];
		feed.reachedEnd = false;
		feed.loadingBatch = false;
		feed.loadBatch();
	},

	active: () => (feed.interval != null),

	shouldUpdate: () => (!feed.reachedEnd && $('.media-feed-end').getBoundingClientRect().top < document.body.clientHeight),

	loadBatch: () => {
		// Make sure we don't load a batch if already busy loading one
		if(feed.loadingBatch) return;
		feed.loadingBatch = true;

		// Add loading icon
		$('.media-feed-end').append(create('div', '', { 'class': 'loading' }));

		// Load media
		const start = document.querySelectorAll('.media-feed > div').length;
		api.search(feed.currentFilters, start, feed.MEDIA_PER_BATCH, (response) => {
			// If no media were returned, we reached the end of the stream
			if(response.media.length == 0) {
				feed.reachedEnd = true;
				clear($('.media-feed-end'));
				$('.media-feed-end').append(create('span', '~', { 'class': 'text-end-of-feed' }));
				return;
			}

			// Add media to feed
			for(var i = 0;i < response.media.length; ++i) {
				// Set picture content
				const type = response.media[i].type;
				const medium = create('div', '', { 'class': type });
				const index = start + i;
				api.media(response.media[i].id, 'small', (data) => {
					medium.style.backgroundImage = 'url(' + data + ')';
					onClick(medium, () => overlay.show(index));
				});
				feed.media.push({
					id: response.media[i].id,
					type: type,
					tagged: response.media[i].tagged.map(personId => data.get('person' + personId, 'name'))
				});
				$('.media-feed').append(medium);
			}

			// Remove loading icon
			clear($('.media-feed-end'));

			// Load new batch if necessary, otherwise indicate no batch is being loaded anymore
			if(feed.shouldUpdate())
				feed.loadBatch();
			else
				feed.loadingBatch = false;
		});
	}

};
