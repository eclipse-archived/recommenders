function(doc) {
	if(!doc.symbolicName || !doc.versionRange || !doc.fingerprints)
		return;

	for(var i=0; i<doc.fingerprints.length; i++) {
		emit(doc.fingerprints[i], doc);
	}
}