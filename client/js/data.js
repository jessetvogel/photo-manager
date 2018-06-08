const data = {

	// Variables
	data: {},

	// Methods
	get: (id, key) => {
		if(data.data[id] == undefined) data.data[id] = {};
			if(data.data[id][key] == undefined) return null;
			return data.data[id][key];		
	},

	set: (id, key, value) => {
		if(data.data[id] == undefined) data.data[id] = {};
			data.data[id][key] = value;
	},

	getPersonIdByName: (name) => {
		for(var key in data.data) {
			if(key.startsWith('person')) {
				if(data.data[key].name == name)
					return key.substring(6);
			}
		}
		return null;
	},





	// Misschien deze functie(s) ergens anders
	updatePeople: (callback) => {
		// Get list of people
		api.people((response) => {
			for(var i = 0;i < response.length; ++i) {
				// Store people data
				data.set('person' + response[i].id, 'name', response[i].name);
				data.set('person' + response[i].id, 'profilePicture', response[i].profilePicture);

				// Add to tag options
				tag.addOption(response[i].name);
			}
			if(callback != undefined) callback();
		});
	},

	updateAlbums: (callback) => {
		// Get list of albums
		api.albums((response) => {
			for(var i = 0;i < response.length; ++i) {
				// Store album data
				data.set('album' + response[i].id, 'title', response[i].title);
				data.set('album' + response[i].id, 'coverPicture', null);
			}
			if(callback != undefined) callback();
		});
	}

};

