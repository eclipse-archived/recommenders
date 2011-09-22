function(doc) {
	if(!doc.imports || !doc.primaryType)
		return;

	for(var i=0; i<doc.imports.length; i++) {
		var _import = doc.imports[i];
		if(_import.fingerprint) {
			emit(_import.fingerprint, doc.creationTimestamp);
		}
	}
}





function (key, values, rereduce) {
	if(values.length > 0) {
		values = values.sort();
		return values[values.length-1];
	}
	else
		return values;
}