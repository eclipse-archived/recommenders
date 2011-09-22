function(doc) {
	if(!doc.symbolicName || !doc.versionRange)
		return;

	emit(doc.symbolicName, doc);

	if(doc.aliases) {
		for(var i=0; i<doc.aliases.length; i++) {
			emit(doc.aliases[i], doc);
		}
	}
}