function(doc) {
	if(doc.name && doc.version) {
		emit(doc._id, doc);
	}
}