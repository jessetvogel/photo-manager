const focus = {

	list: [],

	open: (object) => {
		object.open();
		focus.list.push(object);
	},

	close: () => focus.list.pop().close(),

	current: () => focus.list[focus.list.length - 1]

};


window.


window.onkeydown = (event) => {
	var key = event.keyCode ? event.keyCode : event.which;
	var current = focus.current();

	// Escape -> close current
	if(key == 27) return focus.close();

	// Specifics
	if(current == overlay) {
		if(key == 37) overlay.previous();
		if(key == 39) overlay.next();
		if(key == 84) setTimeout(() => $('.tag-button').click(), 1);
		return;
	}

};
