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
	}

};

