function searchMatch(string, searchTerm) {
  return simplifyString(string).includes(simplifyString(searchTerm));
}

function simplifyString(str) {
  return str.toLowerCase()
            .replace(/[áàâä]/g, 'a')
            .replace(/[úùûü]/g, 'u')
            .replace(/[éèêë]/g, 'e')
            .replace(/[íìîï]/g, 'i')
            .replace(/[óòôö]/g, 'o')
            .replace(/[^A-Za-z0-9\-_]/g, '-');
}
