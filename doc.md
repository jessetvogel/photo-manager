### Data files

- _data/people/people.txt_: contains table with data of all people, i.e. id, name, etc. 
- _data/people/**id**.txt_: contains list of photo id's that are associated to the person with **id**.
- _data/albums/albums.txt_: contains table with data of all albums, i.e. id, title, path, etc.
- _data/albums/**id**.txt_: contains list of photo id's that are contained in the album with **id**.
- _data/pictures/pictures.txt_: contains table with data of all pictures, i.e. id, album id, filename, etc.

### Webserver

- _/_: index page.
- _/js/**…**.js_: javascript files.
- _/css/**…**.css_: css files.

### API-server

- _/health_: returns true if the server is reachable
- _/people_: returns a list of all people.
- _/albums_: returns a list of all people.
- _/search?people=[**id's**]&albums=[**id's**]_: returns list of all pictures associated with the given people and albums.