function(doc) {
	if(!doc.symbolicName || !doc.versionRange)
		return;

	emit(doc.symbolicName, doc);
}