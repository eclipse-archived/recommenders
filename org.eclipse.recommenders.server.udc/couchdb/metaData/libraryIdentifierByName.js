function(doc) {
	if(doc.name && doc.version) {
		emit(doc.name, doc);
	}
}